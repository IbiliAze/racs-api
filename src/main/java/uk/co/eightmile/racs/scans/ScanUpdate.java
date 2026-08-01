package uk.co.eightmile.racs.scans;

import uk.co.eightmile.racs.scans.dtos.ScanDto;

enum ScanAction {created, updated, deleted}

public record ScanUpdate(ScanDto scan, ScanAction action) {}
