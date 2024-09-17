package otp;

import otp.model.Customer;
import otp.model.CustomerSum;
import otp.model.Payment;
import otp.util.CSVReaderUtil;
import otp.util.ReportUtil;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<Customer> customers = CSVReaderUtil.readCustomers();
    List<Payment> payments = CSVReaderUtil.readPayments(customers);
    List<CustomerSum> customerSums = ReportUtil.sumByCustomerReport(customers, payments);
    ReportUtil.reportTopNCustomers(customerSums, 2);
    ReportUtil.reportByWebShop(customers, payments);
  }
}
