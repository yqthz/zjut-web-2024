package service;

import dao.SalaryDao;
import model.Salary;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

public class SalaryService {
    private SalaryDao salaryDao = new SalaryDao();
    public Map<String, Double> getSalaryStats() {
        Map<String, Double> stats = new HashMap<>();
        // 工资区间在low ,high之间的人数
        String[] s = {"3k-5k", "5k-8k", "8k-12k", "12k-20k"};
        // 总人数
        int[] lowSalary = {3000, 5000, 8000, 12000};
        int[] highSalary = {5000, 8000, 12000, 20000};
        for (int i = 0; i < 4; i++) {
            stats.put(s[i], show(lowSalary[i], highSalary[i]));
        }

        return stats;
    }

    public List<Salary> searchByDeptName(String deptName) {
        return salaryDao.searchByDeptName(deptName);

    }

    public List<Salary> searchByName(String name) {
        return salaryDao.serchByName(name);
    }

    public List<Salary> selectAll() {
        return salaryDao.selectAll();
    }

    public List<Salary> selectByPage(int page) {
        return salaryDao.selectByPage(page * 10);
    }
    public void uploadExcel(InputStream excelFileStream) throws IOException {
        // 读取Excel文件
        // 传入的excelFileStream创建了一个Workbook实例，这个实例代表了整个Excel工作簿（即Excel文件）
        // XSSFWorkbook是Apache POI中用于处理XLSX格式的类
        Workbook workbook = new XSSFWorkbook(excelFileStream);

        // 从工作簿中获取第一个工作表（Sheet），索引从0开始
        Sheet sheet = workbook.getSheetAt(0);

        List<Salary> list = new ArrayList<>();


        for (Row row : sheet) {
            // Excel的列分别是员工编号,工资所属年份,工资所属月份,基本工资,
            // 加班工资,全勤奖,个人所得税,实发工资

            // 获取单元格数据

            // 获取员工编号
            int empNo = (int)row.getCell(0).getNumericCellValue();

            // 获取工资所属年份
            int year = (int)row.getCell(1).getNumericCellValue();

            // 获取月份
            int month = (int)row.getCell(2).getNumericCellValue();

            // 获取基本工资
            BigDecimal basicSalary = BigDecimal.valueOf(row.getCell(3).getNumericCellValue());

            // 加班工资
            BigDecimal overtimePay = BigDecimal.valueOf(row.getCell(4).getNumericCellValue());

            // 全勤奖
            BigDecimal fullAttendanceBonus = BigDecimal.valueOf(row.getCell(5).getNumericCellValue());

            // 个人所得税
            BigDecimal personalTax = BigDecimal.valueOf(row.getCell(6).getNumericCellValue());

            // 实发工资
            BigDecimal netSalary = BigDecimal.valueOf(row.getCell(7).getNumericCellValue());


            Salary salary = new Salary(empNo,year, month, basicSalary
            , overtimePay, fullAttendanceBonus, personalTax, netSalary);
            list.add(salary);
        }
        workbook.close();
        salaryDao.save(list);
    }
    public void exportExcel(HttpServletResponse resp) throws IOException {
        List<Salary> list = salaryDao.selectAll();
        // 创建excel文件
        Workbook workbook = new XSSFWorkbook();
        // 创建工作簿
        Sheet sheet = workbook.createSheet("Salaries");

        // 添加表头
        // 创建第一行作为表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("员工编号");
        headerRow.createCell(1).setCellValue("工资所属年份");
        headerRow.createCell(2).setCellValue("工资所属月份");
        headerRow.createCell(3).setCellValue("基本工资");
        headerRow.createCell(4).setCellValue("加班工资");
        headerRow.createCell(5).setCellValue("全勤奖");
        headerRow.createCell(6).setCellValue("个人所得税");
        headerRow.createCell(7).setCellValue("实发工资");


        // 添加数据
        int rowNum = 1;
        for (Salary salary : list) {
            // 为每条记录创建新行，并递增行号
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(salary.getEmpNo());
            row.createCell(1).setCellValue(salary.getYear());
            row.createCell(2).setCellValue(salary.getMonth());
            row.createCell(3).setCellValue(String.valueOf(salary.getBasicSalary()));
            row.createCell(4).setCellValue(String.valueOf(salary.getOvertimePay()));
            row.createCell(5).setCellValue(String.valueOf(salary.getFullAttendanceBonus()));
            row.createCell(6).setCellValue(String.valueOf(salary.getPersonalTax()));
            row.createCell(7).setCellValue(String.valueOf(salary.getNetSalary()));

        }

        // 设置响应头信息
        // 设置响应的内容类型为 Excel 文件的 MIME 类型,告诉浏览器响应的内容是一个 Excel 文件
        // 设置 Content-Disposition 头为 attachment(附件)并指定下载文件的名称为 salaries.xlsx
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=salaries.xlsx");
        
        // 获取响应的输出流，用于将数据写入 HTTP 响应
        try (ServletOutputStream out = resp.getOutputStream()) {
            workbook.write(out);
        }

        // 关闭工作簿资源
        workbook.close();
    }

    public void add(Salary salary) {
        salaryDao.add(salary);
    }

    public void delete(int empNo, int year, int month) {
        salaryDao.delete(empNo, year, month);
    }

    public void update(Salary salary) {
        salaryDao.update(salary);
    }

    public double show(int low, int high) {
        double a = salaryDao.selectSalary(low, high);
        double count = salaryDao.count();
        return a / count;
    }

    public List<Salary> searchByDate(Date beginTime, Date endTime) {
        // 将Date转换成Calendar
        // 创建一个 Calendar 对象并设置时间为当前 date
        Calendar calendarBegin = Calendar.getInstance();
        calendarBegin.setTime(beginTime);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(endTime);

        // 获取年份
        int beginYear = calendarBegin.get(Calendar.YEAR);
        int endYear = calendarEnd.get(Calendar.YEAR);

        // 获取月份
        int beginMonth = calendarBegin.get(Calendar.MONTH) + 1;
        int endMonth = calendarEnd.get(Calendar.MONTH) + 1;

        return salaryDao.searchByDate(beginYear, beginMonth, endYear, endMonth);
    }
}
