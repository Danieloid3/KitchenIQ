package com.uni.kitcheniq.service;

import com.uni.kitcheniq.dto.*;
import com.uni.kitcheniq.enums.EmployeeType;
import com.uni.kitcheniq.enums.PurchaseOrderType;
import com.uni.kitcheniq.exception.*;
import com.uni.kitcheniq.mapper.*;
import com.uni.kitcheniq.models.*;
import com.uni.kitcheniq.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class AdminService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final SupplierMapper supplierMapper;
    private final ShiftChangeRepository shiftChangeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    public String addInventoryItem(InventoryItemDTO inventoryItemDTO) {
        if (inventoryItemDTO == null) {
            throw new NoItemFoundException("Inventory item data is missing");
        }
        InventoryItem item = inventoryItemMapper.toInventoryItem(inventoryItemDTO);

        if (item.getSupplierid() == null) {
            throw new SupplierNotFoundException("Supplier data is missing");
        }

        inventoryItemRepository.save(item);
        return String.format("Successfully added item: %s", item.getName());
    }

    public List<InventoryItemDTO> getAllInventoryItems() {
        List<InventoryItem> items = inventoryItemRepository.findAll();
        List<InventoryItemDTO> itemDTOs = new ArrayList<>();
        for (InventoryItem item : items) {
            itemDTOs.add(inventoryItemMapper.toInventoryItemDTO(item));
        }

        return itemDTOs;
    }

    public PurchaseOrderDTO createPurchaseOrder(SupplierDTO supplierDTO) {
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .status(PurchaseOrderType.PENDING)
                .totalAmount(0.0)
                .build();

        Optional<Supplier> supplier = supplierRepository.findById(supplierDTO.getId());
        if (supplier.isPresent()) {
            purchaseOrder.setSupplier(supplier.get());
        } else {
            throw new SupplierNotFoundException("Supplier not found with ID: " + supplierDTO.getId());
        }

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        PurchaseOrderDTO orderDTO = purchaseOrderMapper.toDTO(saved);

        return orderDTO;
    }

    public PurchaseOrderDTO addItemsToOrder(PurchaseOrderItemDTO itemDTO) {
        PurchaseOrderItem item = purchaseOrderItemMapper.toEntity(itemDTO);
        purchaseOrderItemRepository.save(item);
        purchaseOrderRepository.addToTotalAmountById(item.getPurchaseOrder().getId(), item.getSubTotalPrice());
        PurchaseOrder saved = purchaseOrderRepository.findPurchaseOrderByIdWithItems(item.getPurchaseOrder().getId());

        PurchaseOrderDTO orderDTO = purchaseOrderMapper.toDTO(saved);
        return orderDTO;
    }

    public PurchaseOrderDTO eliminateItemsFromOrder(PurchaseOrderItemDTO itemDTO) {
        purchaseOrderItemRepository.deleteItemFromOrder(itemDTO.getItemId(), itemDTO.getOrderId());
        purchaseOrderRepository.updateTotalPriceById(itemDTO.getOrderId(), itemDTO.getSubTotal());

        PurchaseOrder saved = purchaseOrderRepository.findPurchaseOrderByIdWithItems(itemDTO.getOrderId());
        PurchaseOrderDTO orderDTO = purchaseOrderMapper.toDTO(saved);
        return orderDTO;
    }

    @Transactional
    public PurchaseOrderDTO finalizePurchaseOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findPurchaseOrderByIdWithItems(orderId);
        if (order.getTotalAmount() == 0) {
            purchaseOrderRepository.updateStatusById(orderId, PurchaseOrderType.CANCELLED);
            em.flush();
            em.refresh(order);
        }

        PurchaseOrder saved = purchaseOrderRepository.findPurchaseOrderByIdWithItems(orderId);
        PurchaseOrderDTO orderDTO = purchaseOrderMapper.toDTO(saved);
        return orderDTO;
    }

    @Transactional
    public String cancelPurchaseOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findPurchaseOrderByIdWithItems(orderId);
        for (PurchaseOrderItem item : order.getItems()) {
            purchaseOrderItemRepository.deleteItemFromOrder(item.getId(), orderId);
        }
        purchaseOrderRepository.deletePurchaseOrderById(orderId);
        return "Purchase order cancelled successfully";
    }

    public List<InventoryItemDTO> searchItemsByName (String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllInventoryItems();
        }
        List<InventoryItem> items = inventoryItemRepository.findByNameContainingIgnoreCase(name.trim());
        List<InventoryItemDTO> result = new ArrayList<>();
        for (InventoryItem item : items) {
            result.add(inventoryItemMapper.toInventoryItemDTO(item));
        }
        return result;
    }

    public List<EmployeeDTO> getAllEmployees() {
        Optional<List<Employee>> employees = employeeRepository.getAllEmployees();
        if (!employees.isPresent()) {
           throw new NotEmployees("No employees found");
        }
        List<EmployeeDTO> employeeDTOs = new ArrayList<>();
        for (Employee emp : employees.get()) {
            employeeDTOs.add(employeeMapper.toEmployeeDTO(emp));
        }
        return employeeDTOs;
    }

    @Transactional
    public String deleteEmployee(String id){
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()){
            employeeRepository.deleteById(id);
            return "Employee deleted successfully";
        } else {
            throw new NotEmployees("Employee not found with ID: " + id);
        }
    }

    public List<SupplierDTO> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        if (suppliers.isEmpty()) {
            throw new SupplierNotFoundException("No suppliers found");
        } else {
            List<SupplierDTO> supplierDTOs = new ArrayList<>();
            for (Supplier sup : suppliers) {
                supplierDTOs.add(supplierMapper.toSupplierDTO(sup));
            }
            return supplierDTOs;
        }
    }

    public List<InventoryItemDTO> getInventoryItemsBySupplierId(String supplierId) {
        List<InventoryItem> items = inventoryItemRepository.getInventoryItemsBySupplierId(supplierId);
        if (items.isEmpty()) {
            throw new NoItemFoundException("No inventory items found for supplier ID: " + supplierId);
        }
        List<InventoryItemDTO> itemDTOs = new ArrayList<>();
        for (InventoryItem item : items) {
            itemDTOs.add(inventoryItemMapper.toInventoryItemDTO(item));
        }
        return itemDTOs;
    }

    // ==================== Employee Management - Historia 1: New Employee Registration ====================

    public EmployeeDTO registerEmployee(CreateEmployeeDTO createEmployeeDTO) {
        validateCreateEmployeeDTO(createEmployeeDTO);

        if (employeeRepository.existsById(createEmployeeDTO.getIdNumber())) {
            throw new EmployeeValidationException("Employee with ID number " + createEmployeeDTO.getIdNumber() + " already exists");
        }

        String encodedPassword = passwordEncoder.encode(createEmployeeDTO.getPassword());
        Employee employee = employeeMapper.toEmployee(createEmployeeDTO, encodedPassword);
        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toEmployeeDTO(savedEmployee);
    }

    private void validateCreateEmployeeDTO(CreateEmployeeDTO dto) {
        if (dto == null) {
            throw new EmployeeValidationException("Employee data is missing");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new EmployeeValidationException("Employee name is required");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new EmployeeValidationException("Employee last name is required");
        }
        if (dto.getIdNumber() == null || dto.getIdNumber().trim().isEmpty()) {
            throw new EmployeeValidationException("Employee ID number is required");
        }
        if (!dto.getIdNumber().matches("^[0-9]+$")) {
            throw new EmployeeValidationException("Employee ID number must contain only numbers");
        }
        if (dto.getPosition() == null) {
            throw new EmployeeValidationException("Employee position is required");
        }
        if (dto.getHourlyRate() == null) {
            throw new EmployeeValidationException("Employee hourly rate is required");
        }
        if (dto.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new EmployeeValidationException("Employee hourly rate must be a positive value");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new EmployeeValidationException("Employee password is required");
        }
    }

    // ==================== Employee Management - Historia 2: Employee Information Editing ====================

    public EmployeeDTO getEmployeeById(String employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            throw new EmployeeNotFoundException("Employee not found with ID: " + employeeId);
        }
        return employeeMapper.toEmployeeDTO(employee.get());
    }

    @Transactional
    public EmployeeDTO updateEmployee(String employeeId, UpdateEmployeeDTO updateEmployeeDTO) {
        validateUpdateEmployeeDTO(updateEmployeeDTO);

        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            throw new EmployeeNotFoundException("Employee not found with ID: " + employeeId);
        }

        Employee employee = optionalEmployee.get();
        
        if (updateEmployeeDTO.getName() != null && !updateEmployeeDTO.getName().trim().isEmpty()) {
            employee.setName(updateEmployeeDTO.getName());
        }
        if (updateEmployeeDTO.getLastName() != null && !updateEmployeeDTO.getLastName().trim().isEmpty()) {
            employee.setLastName(updateEmployeeDTO.getLastName());
        }
        if (updateEmployeeDTO.getPosition() != null) {
            employee.setType(updateEmployeeDTO.getPosition());
        }
        if (updateEmployeeDTO.getHourlyRate() != null) {
            employee.setHourlyRate(updateEmployeeDTO.getHourlyRate());
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toEmployeeDTO(savedEmployee);
    }

    private void validateUpdateEmployeeDTO(UpdateEmployeeDTO dto) {
        if (dto == null) {
            throw new EmployeeValidationException("Update data is missing");
        }
        if (dto.getHourlyRate() != null && dto.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new EmployeeValidationException("Employee hourly rate must be a positive value");
        }
    }

    // ==================== Employee Management - Historia 3: Shift Change ====================

    @Transactional
    public ShiftChangeResponseDTO registerShiftChange(ShiftChangeDTO shiftChangeDTO) {
        validateShiftChangeDTO(shiftChangeDTO);

        Optional<Employee> outgoingEmployee = employeeRepository.findById(shiftChangeDTO.getOutgoingEmployeeId());
        Optional<Employee> incomingEmployee = employeeRepository.findById(shiftChangeDTO.getIncomingEmployeeId());

        if (outgoingEmployee.isEmpty()) {
            throw new EmployeeNotFoundException("Outgoing employee not found with ID: " + shiftChangeDTO.getOutgoingEmployeeId());
        }
        if (incomingEmployee.isEmpty()) {
            throw new EmployeeNotFoundException("Incoming employee not found with ID: " + shiftChangeDTO.getIncomingEmployeeId());
        }

        if (outgoingEmployee.get().getType() == EmployeeType.ADMIN) {
            throw new InvalidShiftChangeException("Outgoing employee cannot be an ADMIN. Current position: " + outgoingEmployee.get().getType());
        }
        if (incomingEmployee.get().getType() == EmployeeType.ADMIN) {
            throw new InvalidShiftChangeException("Incoming employee cannot be an ADMIN. Current position: " + incomingEmployee.get().getType());
        }

        LocalDateTime changeDateTime = LocalDateTime.now();

        ShiftChange shiftChange = ShiftChange.builder()
                .outgoingEmployee(outgoingEmployee.get())
                .incomingEmployee(incomingEmployee.get())
                .changeDateTime(changeDateTime)
                .build();

        ShiftChange savedShiftChange = shiftChangeRepository.save(shiftChange);

        return ShiftChangeResponseDTO.builder()
                .id(savedShiftChange.getId())
                .outgoingEmployeeId(outgoingEmployee.get().getId())
                .outgoingEmployeeName(formatEmployeeName(outgoingEmployee.get()))
                .incomingEmployeeId(incomingEmployee.get().getId())
                .incomingEmployeeName(formatEmployeeName(incomingEmployee.get()))
                .changeDateTime(changeDateTime)
                .message("Shift change registered successfully")
                .build();
    }

    private String formatEmployeeName(Employee employee) {
        String firstName = employee.getName() != null ? employee.getName().trim() : "";
        String lastName = employee.getLastName() != null ? employee.getLastName().trim() : "";
        return (firstName + " " + lastName).trim();
    }

    private void validateShiftChangeDTO(ShiftChangeDTO dto) {
        if (dto == null) {
            throw new InvalidShiftChangeException("Shift change data is missing");
        }
        if (dto.getOutgoingEmployeeId() == null || dto.getOutgoingEmployeeId().trim().isEmpty()) {
            throw new InvalidShiftChangeException("Outgoing employee ID is required");
        }
        if (dto.getIncomingEmployeeId() == null || dto.getIncomingEmployeeId().trim().isEmpty()) {
            throw new InvalidShiftChangeException("Incoming employee ID is required");
        }
        if (dto.getOutgoingEmployeeId().equals(dto.getIncomingEmployeeId())) {
            throw new InvalidShiftChangeException("Outgoing and incoming employees must be different");
        }
    }

}
