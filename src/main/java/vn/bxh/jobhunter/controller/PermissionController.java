package vn.bxh.jobhunter.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Permission;
import vn.bxh.jobhunter.service.PermissionService;
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping("/permissions")
    public ResponseEntity<Permission> CreatePermission(@Valid @RequestBody Permission permission){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.HandleCreatePermission(permission));
    }

    @PutMapping("/permissions")
    public ResponseEntity<Permission> UpdatePermission(@Valid @RequestBody Permission permission){
        return ResponseEntity.ok(this.permissionService.HandleUpdatePermission(permission));
    }

    @GetMapping("/permissions/{id}")
    public ResponseEntity<Permission> GetPermission(@Valid @PathVariable long id){
        return ResponseEntity.ok(this.permissionService.GetPermission(id));
    }

    @DeleteMapping("permissions/{id}")
    public ResponseEntity<Void> DeletePermission(@PathVariable long id){
        this.permissionService.DeletePermission(id);
        return ResponseEntity.ok(null);
    }
}
