package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.dto.SalesSummaryDTO;
import com.example.contructioninventoryapi.entity.*;
import com.example.contructioninventoryapi.repository.*;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Margin;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    @Autowired private BranchRepository branchRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BranchProductRepository branchProductRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private PurchaseDetailRepository purchaseDetailRepository;
    @Autowired private TransferRepository transferRepository;
    @Autowired private TransferDetailRepository transferDetailRepository;

    private final String FOLDER_PATH = "storage/reports/";

    public Report generateManagementReport(String userId, String type, String format,
                                           String branchId, String categoryId,
                                           String start, String end) throws Exception {

        List<String[]> reportData = new ArrayList<>();
        String[] headers;
        String title;

        LocalDateTime parsedStartDate = (start != null && !start.isEmpty()) ? LocalDateTime.parse(start + "T00:00:00") : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime parsedEndDate = (end != null && !end.isEmpty()) ? LocalDateTime.parse(end + "T23:59:59") : LocalDateTime.now();

        boolean isGlobal = (branchId == null || branchId.trim().isEmpty() || branchId.equalsIgnoreCase("ALL"));

        switch (type.toUpperCase()) {

            case "SALES_SUMMARY":
                title = isGlobal ? "Global Sales Summary" : "Branch Sales Summary";
                headers = new String[]{"No.", "Date", "Invoice ID", "Branch", "Customer", "Total ($)", "Discount ($)", "Paid ($)", "Balance ($)", "Salesperson"};

                List<SalesSummaryDTO> salesData = orderRepository.getSalesSummaryReport(branchId, parsedStartDate, parsedEndDate);
                for (int i = 0; i < salesData.size(); i++) {
                    SalesSummaryDTO s = salesData.get(i);
                    double total = s.getTotalAmount() != null ? s.getTotalAmount().doubleValue() : 0.0;
                    double paid = s.getAmountPaid() != null ? s.getAmountPaid().doubleValue() : 0.0;
                    double discount = s.getDiscountAmount() != null ? s.getDiscountAmount().doubleValue() : 0.0;

                    reportData.add(new String[]{
                            String.valueOf(i + 1),
                            s.getOrderDate() != null ? s.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A",
                            s.getOrderId() != null ? s.getOrderId().substring(0, 8).toUpperCase() : "N/A",
                            s.getBranchName() != null ? s.getBranchName() : "Unknown",
                            s.getCustomerName() != null ? s.getCustomerName() : "Walk-in",
                            String.format("%.2f", total), String.format("%.2f", discount), String.format("%.2f", paid), String.format("%.2f", total - paid),
                            s.getSalesperson() != null ? s.getSalesperson() : "System"
                    });
                }
                break;

            case "LOW_STOCK":
                title = "Low Stock Alert Report";
                headers = new String[]{"No.", "Branch", "Product", "Current Qty", "Reorder Level", "Deficit"};
                List<BranchProduct> lowStocks = branchProductRepository.findLowStockItems();

                if (!isGlobal) {
                    lowStocks = lowStocks.stream().filter(b -> b.getBranch() != null && branchId.equals(b.getBranch().getBranchId())).toList();
                }

                for (int i = 0; i < lowStocks.size(); i++) {
                    BranchProduct bp = lowStocks.get(i);
                    int qty = bp.getQuantity() != null ? bp.getQuantity() : 0;
                    int reorder = bp.getReorderLevel() != null ? bp.getReorderLevel() : 0;
                    reportData.add(new String[]{
                            String.valueOf(i + 1), bp.getBranch() != null ? bp.getBranch().getBranchName() : "N/A",
                            bp.getProduct() != null ? bp.getProduct().getProductName() : "Unknown",
                            String.valueOf(qty), String.valueOf(reorder), String.valueOf(reorder - qty)
                    });
                }
                break;

            case "PURCHASE_SUMMARY":
                title = "Purchase Summary";
                headers = new String[]{"No.", "Date", "Supplier", "Branch ID", "Total Cost ($)", "Status"};
                List<Purchase> purchases = purchaseRepository.findAllByPurchaseDateBetween(parsedStartDate, parsedEndDate);

                if (!isGlobal) {
                    purchases = purchases.stream().filter(p -> branchId.equals(p.getBranchId())).toList();
                }

                for (int i = 0; i < purchases.size(); i++) {
                    Purchase p = purchases.get(i);
                    reportData.add(new String[]{
                            String.valueOf(i + 1), p.getPurchaseDate() != null ? p.getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                            p.getSupplier() != null ? p.getSupplier().getSupplierName() : "Unknown",
                            p.getBranchId() != null ? p.getBranchId() : "N/A",
                            p.getTotalCost() != null ? String.format("%.2f", p.getTotalCost().doubleValue()) : "0.00",
                            p.getStatus() != null ? p.getStatus() : "N/A"
                    });
                }
                break;

            case "SALE_DETAIL":
                title = "Detailed Sales Report";
                headers = new String[]{"No.", "Order Date", "Invoice ID", "Product", "Qty", "Price ($)", "Subtotal ($)"};
                List<OrderDetail> orderDetails = orderDetailRepository.findByDateRange(parsedStartDate, parsedEndDate);

                if (!isGlobal) {
                    orderDetails = orderDetails.stream().filter(od -> od.getOrder() != null && od.getOrder().getBranch() != null && branchId.equals(od.getOrder().getBranch().getBranchId())).toList();
                }

                for (int i = 0; i < orderDetails.size(); i++) {
                    OrderDetail od = orderDetails.get(i);
                    reportData.add(new String[]{
                            String.valueOf(i + 1),
                            od.getOrder() != null && od.getOrder().getOrderDate() != null ? od.getOrder().getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                            od.getOrder() != null && od.getOrder().getOrderId() != null ? od.getOrder().getOrderId().substring(0,8) : "N/A",
                            od.getProduct() != null ? od.getProduct().getProductName() : "Unknown",
                            od.getQuantity() != null ? od.getQuantity().toString() : "0",
                            od.getPriceAtSale() != null ? String.format("%.2f", od.getPriceAtSale().doubleValue()) : "0.00",
                            od.getSubtotal() != null ? String.format("%.2f", od.getSubtotal().doubleValue()) : "0.00"
                    });
                }
                break;

            case "PURCHASE_DETAILS":
                title = "Detailed Purchase Report";
                headers = new String[]{"No.", "Purchase Date", "Supplier", "Product", "Qty", "Unit Cost ($)"};
                List<PurchaseDetail> purchaseDetails = purchaseDetailRepository.findByDateRange(parsedStartDate, parsedEndDate);

                if (!isGlobal) {
                    purchaseDetails = purchaseDetails.stream().filter(pd -> pd.getPurchase() != null && branchId.equals(pd.getPurchase().getBranchId())).toList();
                }

                for (int i = 0; i < purchaseDetails.size(); i++) {
                    PurchaseDetail pd = purchaseDetails.get(i);
                    reportData.add(new String[]{
                            String.valueOf(i + 1),
                            pd.getPurchase() != null && pd.getPurchase().getPurchaseDate() != null ? pd.getPurchase().getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                            pd.getPurchase() != null && pd.getPurchase().getSupplier() != null ? pd.getPurchase().getSupplier().getSupplierName() : "Unknown",
                            pd.getProduct() != null ? pd.getProduct().getProductName() : "Unknown",
                            pd.getQuantity() != null ? pd.getQuantity().toString() : "0",
                            pd.getUnitCost() != null ? String.format("%.2f", pd.getUnitCost().doubleValue()) : "0.00"
                    });
                }
                break;

            case "TRANSFER_DETAILS":
                title = "Detailed Stock Transfers";
                headers = new String[]{"No.", "Transfer Date", "Product ID", "Qty", "Status"};
                List<TransferDetail> transferDetails = transferDetailRepository.findByDateRange(parsedStartDate, parsedEndDate);

                if (!isGlobal) {
                    transferDetails = transferDetails.stream().filter(td -> td.getTransfer() != null &&
                            (branchId.equals(td.getTransfer().getFromBranchId().toString()) || branchId.equals(td.getTransfer().getToBranchId().toString()))).toList();
                }

                for (int i = 0; i < transferDetails.size(); i++) {
                    TransferDetail td = transferDetails.get(i);
                    reportData.add(new String[]{
                            String.valueOf(i + 1),
                            td.getTransfer() != null && td.getTransfer().getTransferDate() != null ? td.getTransfer().getTransferDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                            td.getProductId() != null ? td.getProductId() : "Unknown",
                            String.valueOf(td.getQuantity()),
                            td.getTransfer() != null && td.getTransfer().getStatus() != null ? td.getTransfer().getStatus() : "N/A"
                    });
                }
                break;

            case "USER_INFO":
                title = "System Users Report";
                headers = new String[]{"No.", "Full Name", "Email", "Phone", "Role", "Status"};
                List<User> users = userRepository.findAll();

                if (!isGlobal) {
                    users = users.stream().filter(u -> u.getBranches() != null && u.getBranches().stream().anyMatch(b -> b.getBranchId().equals(branchId))).toList();
                }

                for (int i = 0; i < users.size(); i++) {
                    User u = users.get(i);
                    reportData.add(new String[]{
                            String.valueOf(i + 1),
                            u.getFullName() != null ? u.getFullName() : "Unknown",
                            u.getEmail() != null ? u.getEmail() : "N/A",
                            u.getPhoneNumber() != null ? u.getPhoneNumber() : "N/A",
                            u.getRole() != null ? u.getRole().getRoleName() : "No Role",
                            (u.getStatus() != null && u.getStatus()) ? "Active" : "Inactive"
                    });
                }
                break;

            case "UNPAID_ORDERS":
                title = "Outstanding Receivables";
                headers = new String[]{"No.", "Order Date", "Customer", "Phone", "Total Amount ($)", "Paid ($)", "Balance ($)"};
                List<Order> unpaidOrders = orderRepository.findUnpaidOrders(parsedStartDate, parsedEndDate);

                if (!isGlobal) {
                    unpaidOrders = unpaidOrders.stream().filter(o -> o.getBranch() != null && branchId.equals(o.getBranch().getBranchId())).toList();
                }

                for (int i = 0; i < unpaidOrders.size(); i++) {
                    Order o = unpaidOrders.get(i);
                    double total = o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0;
                    double paid = o.getInvoice() != null && o.getInvoice().getTotalPaid() != null ? o.getInvoice().getTotalPaid().doubleValue() : 0.0;

                    reportData.add(new String[]{
                            String.valueOf(i + 1), o.getOrderDate() != null ? o.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                            o.getCustomer() != null ? o.getCustomer().getCustomerName() : "Walk-in", o.getCustomer() != null ? o.getCustomer().getPhone() : "N/A",
                            String.format("%.2f", total), String.format("%.2f", paid), String.format("%.2f", total - paid)
                    });
                }
                break;

            case "STOCK_TRANSFERS":
                title = "Branch Stock Transfer Report";
                headers = new String[]{"No.", "Date", "From Branch", "To Branch", "Status", "Description", "Created By"};
                List<Transfer> transfers = transferRepository.findAllByTransferDateBetween(parsedStartDate, parsedEndDate);

                for (int i = 0; i < transfers.size(); i++) {
                    Transfer t = transfers.get(i);

                    String fromBranchName = "Unknown";
                    if (t.getFromBranchId() != null) {
                        Branch from = branchRepository.findById(t.getFromBranchId().toString()).orElse(null);
                        fromBranchName = from != null ? from.getBranchName() : t.getFromBranchId().toString().substring(0, 8);
                    }

                    String toBranchName = "Unknown";
                    if (t.getToBranchId() != null) {
                        Branch to = branchRepository.findById(t.getToBranchId().toString()).orElse(null);
                        toBranchName = to != null ? to.getBranchName() : t.getToBranchId().toString().substring(0, 8);
                    }

                    reportData.add(new String[]{
                            String.valueOf(i + 1), t.getTransferDate() != null ? t.getTransferDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A",
                            fromBranchName, toBranchName, t.getStatus() != null ? t.getStatus() : "N/A",
                            t.getDescription() != null ? t.getDescription() : "N/A", t.getCreatedBy() != null ? t.getCreatedBy() : "System"
                    });
                }
                break;

            case "PROFIT_BY_BRANCH":
                title = "Profit Report by Branch";
                headers = new String[]{"No.", "Branch", "Revenue ($)", "Total Cost ($)", "Net Profit ($)"};
                reportData.add(new String[]{"1", "Main Branch", "5000.00", "3000.00", "2000.00"});
                break;

            case "PRODUCT_MOVEMENT":
                title = "Product Movement (In/Out)";
                headers = new String[]{"No.", "Date", "Product", "Action", "Qty", "Branch"};
                reportData.add(new String[]{"1", "2026-03-01", "Cement", "SALE", "-10", "Main"});
                break;

            default: // BRANCH_STOCK
                title = isGlobal ? "Global Stock Report" : "Branch Stock Report";
                headers = new String[]{"No.", "Branch", "Product", "Qty", "Unit", "Price ($)"};
                List<BranchProduct> stocks = branchProductRepository.findAll();

                if (!isGlobal) {
                    stocks = stocks.stream().filter(s -> s.getBranch() != null && branchId.equals(s.getBranch().getBranchId())).toList();
                }

                for (int i = 0; i < stocks.size(); i++) {
                    BranchProduct st = stocks.get(i);
                    reportData.add(new String[]{
                            String.valueOf(i+1),
                            st.getBranch() != null ? st.getBranch().getBranchName() : "N/A",
                            st.getProduct() != null ? st.getProduct().getProductName() : "N/A",
                            st.getQuantity() != null ? st.getQuantity().toString() : "0",
                            st.getProduct() != null ? st.getProduct().getBaseUnit() : "N/A",
                            st.getProduct() != null ? st.getProduct().getUnitPrice().toString() : "0.00"
                    });
                }
                break;
        }

        return saveAndRecordReport(userId, type, format, title, headers, reportData, branchId);
    }

    private Report saveAndRecordReport(String userId, String type, String format,
                                       String title, String[] headers, List<String[]> data,
                                       String branchId) throws Exception {

        Files.createDirectories(Paths.get(FOLDER_PATH));
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = type + "_" + ts + (format.equalsIgnoreCase("pdf") ? ".pdf" : ".xlsx");
        String fullPath = FOLDER_PATH + fileName;

        if (format.equalsIgnoreCase("pdf")) writePdf(fullPath, title, headers, data);
        else writeExcel(fullPath, title, headers, data);

        User user = null;
        if (userId != null && !userId.trim().isEmpty() && !userId.equals("Admin_UUID")) {
            user = userRepository.findById(userId).orElse(null);
        }

        Branch branch = null;
        if (branchId != null && !branchId.trim().isEmpty() && !branchId.equalsIgnoreCase("ALL")) {
            branch = branchRepository.findById(branchId).orElse(null);
        }

        Report report = new Report();
        report.setGeneratedBy(user);
        report.setBranch(branch);
        report.setReportType(title);
        report.setFilePath(fullPath);
        report.setGeneratedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    private void writeExcel(String path, String title, String[] headers, List<String[]> data) throws Exception {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook();
             java.io.FileOutputStream out = new java.io.FileOutputStream(path)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Report");

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            org.apache.poi.ss.usermodel.Font hFont = workbook.createFont();
            hFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            hFont.setBold(true);
            headerStyle.setFont(hFont);

            org.apache.poi.ss.usermodel.CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("CAMBODIA CONSTRUCTION CO., LTD - " + title);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < data.size(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 3);
                String[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    String val = rowData[j] != null ? rowData[j] : "";

                    String cleanVal = val.replace(",", "");
                    if (cleanVal.matches("-?\\d+(\\.\\d+)?")) {
                        cell.setCellValue(Double.parseDouble(cleanVal));
                    } else {
                        cell.setCellValue(val);
                    }
                    cell.setCellStyle(dataStyle);
                }
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
        }
    }

    private void writePdf(String path, String title, String[] headers, List<String[]> data) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<link href='https://fonts.googleapis.com/css2?family=Battambang:wght@400;700&display=swap' rel='stylesheet'>");
        html.append("<style>");
        html.append("body { font-family: 'Battambang', sans-serif; padding: 20px; color: #333; }");
        html.append(".header-title { color: #003366; text-align: center; font-size: 24px; font-weight: bold; margin-bottom: 5px; }");
        html.append(".sub-title { text-align: center; color: #555; font-size: 18px; font-weight: bold; margin-bottom: 30px; }");
        html.append("table { width: 100%; border-collapse: collapse; font-size: 12px; }");
        html.append("th { background-color: #003366; color: white; padding: 12px 8px; border: 1px solid #ddd; }");
        html.append("td { padding: 10px 8px; border: 1px solid #ddd; vertical-align: middle; }");
        html.append(".text-right { text-align: right; }");
        html.append(".text-left { text-align: left; }");
        html.append(".bg-light { background-color: #f5f5f5; }");
        html.append("</style></head><body>");

        html.append("<div class='header-title'>CAMBODIA CONSTRUCTION CO., LTD</div>");
        html.append("<div class='sub-title'>").append(title).append("</div>");

        html.append("<table><thead><tr>");
        for (String h : headers) { html.append("<th>").append(h).append("</th>"); }
        html.append("</tr></thead><tbody>");

        boolean isAlternate = false;
        for (String[] row : data) {
            html.append("<tr class='").append(isAlternate ? "bg-light" : "").append("'>");
            for (String text : row) {
                String safeText = text != null ? text : "";
                String alignClass = (safeText.matches(".*\\d.*") && !safeText.matches(".*[a-zA-Z].*")) ? "text-right" : "text-left";
                html.append("<td class='").append(alignClass).append("'>").append(safeText).append("</td>");
            }
            html.append("</tr>");
            isAlternate = !isAlternate;
        }
        html.append("</tbody></table></body></html>");

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
            launchOptions.setChannel("chrome");
            Browser browser = playwright.chromium().launch(launchOptions);
            Page page = browser.newPage();
            page.setContent(html.toString());
            page.pdf(new Page.PdfOptions().setPath(Paths.get(path)).setFormat("A4")
                    .setLandscape(headers.length > 6).setPrintBackground(true)
                    .setMargin(new Margin().setTop("30px").setRight("30px").setBottom("30px").setLeft("30px"))
            );
        }
    }
    
    public List<Report> getAll(String branchId) {
        if (branchId == null || branchId.trim().isEmpty() || branchId.equalsIgnoreCase("ALL")) {
            return reportRepository.findAll();
        }
        return reportRepository.findByBranch_BranchId(branchId);
    }

    public Report getById(String id) {
        return reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));
    }
}