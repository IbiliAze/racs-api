package uk.co.eightmile.racs.permissions;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Authority> {
}