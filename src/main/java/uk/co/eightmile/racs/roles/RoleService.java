package uk.co.eightmile.racs.roles;

import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.permissions.Authority;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.permissions.PermissionRepository;
import uk.co.eightmile.racs.permissions.exceptions.PermissionNotFoundException;
import uk.co.eightmile.racs.roles.dtos.*;
import uk.co.eightmile.racs.roles.exceptions.RoleNotFoundException;
import uk.co.eightmile.racs.roles.specifications.RoleSpec;
import uk.co.eightmile.racs.roles.dtos.SingleItemResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    public GetRolesResponse getRoles(RoleRequestQueryParams queryParams) {
        QueryBuilder<Role> queryBuilder = new QueryBuilder<>(queryParams);
        Specification<Role> spec = RoleSpec.fromQueryParams(queryParams);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<Role> page = roleRepository.findAll(spec, pageRequest);

        GetRolesResponse response = new GetRolesResponse();
        response.setRoles(page.getContent().stream().map(roleMapper::toDto).toList());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Roles fetched successfully");

        return response;
    }

    public SingleItemResponse getRoleById(UUID id) {
        var role = roleRepository.findById(id).orElseThrow(RoleNotFoundException::new);

        var response = new SingleItemResponse();
        response.setRole(roleMapper.toDto(role));
        response.setMessage("Role fetched successfully");

        return response;
    }

    public List<Permission> getRolePermissions(UUID id) {
        var role = roleRepository.findById(id).orElseThrow(RoleNotFoundException::new);

        return role.getPermissions().stream().toList();
    }

    public SingleItemResponse createRole(CreateRoleRequest request) {
        var role = roleMapper.toEntity(request);
        roleRepository.save(role);

        var response = new uk.co.eightmile.racs.roles.dtos.SingleItemResponse();
        response.setRole(roleMapper.toDto(role));
        response.setMessage("Role created successfully");

        return response;
    }

    public RoleDto updatePermissions(UUID id, UpdateRolePermissionsRequest request) {
        var role = roleRepository.findById(id)
                .orElseThrow(RoleNotFoundException::new);

        var requestedPermissionIds = request.getPermissionIds();

        var permissions = permissionRepository.findAllById(requestedPermissionIds);

        if (permissions.size() != requestedPermissionIds.size()) {
            throw new PermissionNotFoundException();
        }

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);

        roleRepository.save(role);

        return roleMapper.toDto(role);
    }

    public RoleDto removePermission(UUID id, Authority permissionId) {
        var role = roleRepository.findById(id).orElseThrow(RoleNotFoundException::new);

        var permission = permissionRepository.findById(permissionId)
                .orElseThrow(PermissionNotFoundException::new);

        role
                .getPermissions()
                .stream()
                .filter(p -> p.getId().name().equals(permission.getId().name()))
                .findFirst()
                .orElseThrow(PermissionNotFoundException::new);

        role.removePermission(permission);
        roleRepository.save(role);
        return roleMapper.toDto(role);
    }

    public SingleItemResponse updateRole(UUID id, UpdateRoleRequest request) {
        var role = roleRepository.findById(id).orElseThrow(RoleNotFoundException::new);

        roleMapper.update(request, role);
        roleRepository.save(role);

        var response = new uk.co.eightmile.racs.roles.dtos.SingleItemResponse();
        response.setRole(roleMapper.toDto(role));
        response.setMessage("Role updated successfully");

        return response;
    }

    public SingleItemResponse deleteRole(UUID id) {
        var role = roleRepository.findById(id).orElseThrow(RoleNotFoundException::new);

        roleRepository.deleteById(id);

        var response = new uk.co.eightmile.racs.roles.dtos.SingleItemResponse();
        response.setRole(roleMapper.toDto(role));
        response.setMessage("Role deleted successfully");

        return response;
    }
}
