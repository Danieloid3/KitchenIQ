package com.uni.kitcheniq.controller;

import com.uni.kitcheniq.dto.*;
import com.uni.kitcheniq.models.PurchaseOrder;
import com.uni.kitcheniq.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("kitcheniq/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/add-inventory-item")
    public ResponseEntity<String> addInventoryItem(@RequestBody InventoryItemDTO item) {
        String response = adminService.addInventoryItem(item);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory-list")
    public ResponseEntity<List<InventoryItemDTO>> getInventoryItems() {
        List<InventoryItemDTO> items = adminService.getAllInventoryItems();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/initialize-purchase-order")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@RequestBody SupplierDTO supplierDTO) {
        PurchaseOrderDTO order = adminService.createPurchaseOrder(supplierDTO);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/add-items-to-order")
    public ResponseEntity<PurchaseOrderDTO> addItemsToPurchaseOrder(@RequestBody PurchaseOrderItemDTO purchaseOrderItemDTO) {
        PurchaseOrderDTO order = adminService.addItemsToOrder(purchaseOrderItemDTO);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/eliminate-items-from-order")
    public ResponseEntity<PurchaseOrderDTO> eliminateItemsFromOrder(@RequestBody PurchaseOrderItemDTO purchaseOrderItemDTO) {
        PurchaseOrderDTO order = adminService.eliminateItemsFromOrder(purchaseOrderItemDTO);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/cancel-purchase-order")
    public ResponseEntity<String> cancelPurchaseOrder(@RequestParam Long orderId) {
        String message = adminService.cancelPurchaseOrder(orderId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/finalize-purchase-order")
    public ResponseEntity<PurchaseOrderDTO> finalizePurchaseOrder(@RequestParam Long orderId) {
        PurchaseOrderDTO order = adminService.finalizePurchaseOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/inventory-search")
    public ResponseEntity<List<InventoryItemDTO>> searchInventoryItems(
            @RequestParam(name = "name", required = false) String name) {
        return ResponseEntity.ok(adminService.searchItemsByName(name));
    }

    @GetMapping("/employees-list")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesList() {
        List<EmployeeDTO> employees = adminService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @PostMapping("/delete-employee")
    public ResponseEntity<String> deleteEmployee(@RequestParam String employeeId) {
        String response = adminService.deleteEmployee(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supplier-list")
    public ResponseEntity<List<SupplierDTO>> getSuppliersList() {
        List<SupplierDTO> suppliers = adminService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/supplier-inventory-items")
    public ResponseEntity<List<InventoryItemDTO>> getInventoryItemsBySupplierId(@RequestParam String supplierId) {
        List<InventoryItemDTO> items = adminService.getInventoryItemsBySupplierId(supplierId);
        return ResponseEntity.ok(items);
    }

    // ==================== Employee Management - Historia 1: New Employee Registration ====================

    /**
     * Register a new employee in the system.
     * Required fields: name, lastName, idNumber, position, hourlyRate
     * Validations:
     * - idNumber accepts only numbers
     * - hourlyRate accepts only positive values
     * - idNumber must not exist previously
     * The contract date is automatically generated with the current date.
     */
    @PostMapping("/register-employee")
    public ResponseEntity<EmployeeDTO> registerEmployee(@RequestBody CreateEmployeeDTO createEmployeeDTO) {
        EmployeeDTO employee = adminService.registerEmployee(createEmployeeDTO);
        return ResponseEntity.ok(employee);
    }

    // ==================== Employee Management - Historia 2: Employee Information Editing ====================

    /**
     * Get employee details for editing (enter edit mode).
     * Returns the employee information that can be edited.
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployeeForEdit(@PathVariable String employeeId) {
        EmployeeDTO employee = adminService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }

    /**
     * Update employee information (save changes).
     * Editable fields: name, lastName, position, hourlyRate
     * Non-editable fields: idNumber, contractDate
     * Validation: hourlyRate must be positive
     */
    @PutMapping("/employee/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable String employeeId,
            @RequestBody UpdateEmployeeDTO updateEmployeeDTO) {
        EmployeeDTO employee = adminService.updateEmployee(employeeId, updateEmployeeDTO);
        return ResponseEntity.ok(employee);
    }

    /**
     * Cancel employee edit (returns current employee state without changes).
     * This endpoint allows canceling the edit operation by returning the current state.
     */
    @GetMapping("/employee/{employeeId}/cancel-edit")
    public ResponseEntity<EmployeeDTO> cancelEmployeeEdit(@PathVariable String employeeId) {
        EmployeeDTO employee = adminService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }

    // ==================== Employee Management - Historia 3: Shift Change ====================

    /**
     * Register a shift change between two employees.
     * Required fields: outgoingEmployeeId, incomingEmployeeId
     * Validations:
     * - Both IDs must exist
     * - Both employees must have position "EMPLOYEE"
     * The system records the exact date and time of the change.
     */
    @PostMapping("/shift-change")
    public ResponseEntity<ShiftChangeResponseDTO> registerShiftChange(@RequestBody ShiftChangeDTO shiftChangeDTO) {
        ShiftChangeResponseDTO response = adminService.registerShiftChange(shiftChangeDTO);
        return ResponseEntity.ok(response);
    }
}
