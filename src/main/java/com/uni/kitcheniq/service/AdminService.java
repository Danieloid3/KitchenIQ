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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ShiftChangeMapper shiftChangeMapper;
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

    // ========== Story 1: New Employee Registration ==========

    @Transactional
    public EmployeeDTO registerEmployee(CreateEmployeeDTO dto) {
        validateCreateEmployeeDTO(dto);

        if (employeeRepository.existsByIdNumber(dto.getIdNumber())) {
            throw new DuplicateIdNumberException("An employee with this ID number already exists");
        }

        Employee employee = employeeMapper.toEmployee(dto);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toEmployeeDTO(saved);
    }

    private void validateCreateEmployeeDTO(CreateEmployeeDTO dto) {
        if (dto == null) {
            throw new InvalidEmployeeDataException("Employee data is missing");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee name is required");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee last name is required");
        }
        if (dto.getIdNumber() == null || dto.getIdNumber().trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee ID number is required");
        }
        if (!dto.getIdNumber().matches("\\d+")) {
            throw new InvalidEmployeeDataException("ID number must contain only numbers");
        }
        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee position is required");
        }
        if (dto.getHourlyRate() == null || dto.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidEmployeeDataException("Hourly rate must be a positive value");
        }
    }

    // ========== Story 2: Employee Information Editing ==========

    public EmployeeDTO getEmployeeById(String id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()) {
            return employeeMapper.toEmployeeDTO(employee.get());
        } else {
            throw new NotEmployees("Employee not found with ID: " + id);
        }
    }

    @Transactional
    public EmployeeDTO updateEmployee(String id, UpdateEmployeeDTO dto) {
        validateUpdateEmployeeDTO(dto);

        Optional<Employee> existingEmployee = employeeRepository.findById(id);
        if (!existingEmployee.isPresent()) {
            throw new NotEmployees("Employee not found with ID: " + id);
        }

        Employee employee = existingEmployee.get();
        employee.setName(dto.getName());
        employee.setLastName(dto.getLastName());
        employee.setPosition(dto.getPosition());
        employee.setHourlyRate(dto.getHourlyRate());

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toEmployeeDTO(saved);
    }

    private void validateUpdateEmployeeDTO(UpdateEmployeeDTO dto) {
        if (dto == null) {
            throw new InvalidEmployeeDataException("Employee update data is missing");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee name is required");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee last name is required");
        }
        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee position is required");
        }
        if (dto.getHourlyRate() == null || dto.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidEmployeeDataException("Hourly rate must be a positive value");
        }
    }

    // ========== Story 3: Shift Change Management ==========

    @Transactional
    public ShiftChangeDTO createShiftChange(CreateShiftChangeDTO dto) {
        validateShiftChangeDTO(dto);

        Optional<Employee> outgoingEmployee = employeeRepository.findById(dto.getOutgoingEmployeeId());
        Optional<Employee> incomingEmployee = employeeRepository.findById(dto.getIncomingEmployeeId());

        if (!outgoingEmployee.isPresent()) {
            throw new InvalidShiftChangeException("Outgoing employee not found with ID: " + dto.getOutgoingEmployeeId());
        }
        if (!incomingEmployee.isPresent()) {
            throw new InvalidShiftChangeException("Incoming employee not found with ID: " + dto.getIncomingEmployeeId());
        }

        if (!isEmployeePosition(outgoingEmployee.get())) {
            throw new InvalidShiftChangeException("Outgoing employee must have an Employee position (CHEF or WAITER)");
        }
        if (!isEmployeePosition(incomingEmployee.get())) {
            throw new InvalidShiftChangeException("Incoming employee must have an Employee position (CHEF or WAITER)");
        }

        ShiftChange shiftChange = ShiftChange.builder()
                .outgoingEmployee(outgoingEmployee.get())
                .incomingEmployee(incomingEmployee.get())
                .changeDateTime(LocalDateTime.now())
                .notes(dto.getNotes())
                .build();

        ShiftChange saved = shiftChangeRepository.save(shiftChange);
        return shiftChangeMapper.toShiftChangeDTO(saved);
    }

    private boolean isEmployeePosition(Employee employee) {
        return employee.getType() == EmployeeType.CHEF || employee.getType() == EmployeeType.WAITER;
    }

    private void validateShiftChangeDTO(CreateShiftChangeDTO dto) {
        if (dto == null) {
            throw new InvalidShiftChangeException("Shift change data is missing");
        }
        if (dto.getOutgoingEmployeeId() == null || dto.getOutgoingEmployeeId().trim().isEmpty()) {
            throw new InvalidShiftChangeException("Outgoing employee ID is required");
        }
        if (dto.getIncomingEmployeeId() == null || dto.getIncomingEmployeeId().trim().isEmpty()) {
            throw new InvalidShiftChangeException("Incoming employee ID is required");
        }
    }

    public List<ShiftChangeDTO> getAllShiftChanges() {
        List<ShiftChange> shiftChanges = shiftChangeRepository.findAllOrderByChangeDateTimeDesc();
        return shiftChanges.stream()
                .map(shiftChangeMapper::toShiftChangeDTO)
                .collect(Collectors.toList());
    }

}
