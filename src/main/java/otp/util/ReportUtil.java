package otp.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import otp.model.Customer;
import otp.model.CustomerSum;
import otp.model.Payment;
import otp.model.PaymentType;
import otp.model.WebShopSum;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ReportUtil {
  private static final Logger log = LogManager.getLogger(ReportUtil.class);

  private ReportUtil() {
    throw new UnsupportedOperationException("This is a util class!");
  }

  public static List<CustomerSum> sumByCustomerReport(
      List<Customer> customers,
      List<Payment> payments
  ) {
    List<String> reportLines = new ArrayList<>();
    List<CustomerSum> customerSums = calculateCustomerSums(customers, payments);

    for (CustomerSum customerSum : customerSums) {
      reportLines.add(customerSum.getCSVLine());
    }

    try {
      writeToFile(reportLines, "src/reports/report01.csv");
    } catch (IOException e) {
      log.error("Error creating report summarizing customer's spending: {}", e.getMessage());
    }

    return customerSums;
  }

  private static List<CustomerSum> calculateCustomerSums(
      List<Customer> customers,
      List<Payment> payments
  ) {
    List<CustomerSum> customerSums = new ArrayList<>();
    for (Customer customer : customers) {
      BigDecimal sum = BigDecimal.ZERO;
      for (Payment payment : payments) {
        if (payment.getShopId().equals(customer.getShopId()) &&
            payment.getCustomerId().equals(customer.getCustomerId())) {
          sum = sum.add(payment.getAmount());
        }
      }
      CustomerSum customerSum = new CustomerSum(customer.getName(), customer.getAddress(), sum);
      customerSums.add(customerSum);
    }
    return customerSums;
  }

  public static void reportTopNCustomers(List<CustomerSum> customerSums, int numberOfCustomers) {
    if (customerSums.isEmpty()) {
      log.info("There's nothing to report, the list of sums is empty");
      return;
    }

    if (numberOfCustomers < 1) {
      log.info("There's nothing to report, the number of top users is: {}", numberOfCustomers);
      return;
    }

    List<String> reportLines = getTopLines(customerSums, numberOfCustomers);
    try {
      writeToFile(reportLines, "src/reports/top.csv");
    } catch (IOException e) {
      log.error("Error creating report summarizing customer's spending: {}", e.getMessage());
    }
  }

  private static List<String> getTopLines(
      List<CustomerSum> customerSums,
      Integer numberOfCustomers
  ) {
    Collections.sort(customerSums, new Comparator<CustomerSum>() {
      @Override
      public int compare(CustomerSum u1, CustomerSum u2) {
        return u2.getSum().compareTo(u1.getSum());
      }
    });

    List<String> reportLines = new ArrayList<>();
    for (int i = 0; i < numberOfCustomers; i++) {
      reportLines.add(customerSums.get(i).getCSVLine());
    }
    return reportLines;
  }

  public static void reportByWebShop(
      List<Customer> customers,
      List<Payment> payments
  ) {
    List<String> reportLines = new ArrayList<>();
    for (WebShopSum webshopSum : calculateShopSums(customers, payments)) {
      reportLines.add(webshopSum.getCSVLine());
    }

    try {
      writeToFile(reportLines, "src/reports/report02.csv");
    } catch (IOException e) {
      log.error("Error creating report summarizing webshops transactions: {}", e.getMessage());
    }
  }

  private static List<WebShopSum> calculateShopSums(
      List<Customer> customers,
      List<Payment> payments
  ) {
    List<WebShopSum> webShopSums = new ArrayList<>();
    SortedSet<String> webshops = new TreeSet<>();
    for (Customer customer : customers) {
      webshops.add(customer.getShopId());
    }

    for (String webshop : webshops) {
      BigDecimal sumCards = BigDecimal.ZERO;
      BigDecimal sumTransfers = BigDecimal.ZERO;
      for (Payment payment : payments) {
        if (payment.getShopId().equals(webshop)) {
          if (payment.getType() == PaymentType.CARD) {
            sumCards = sumCards.add(payment.getAmount());
          } else {
            sumTransfers = sumTransfers.add(payment.getAmount());
          }
        }
      }
      webShopSums.add(new WebShopSum(webshop, sumCards, sumTransfers));
    }
    return webShopSums;
  }

  private static void writeToFile(List<String> lines, String fileName) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
      for (String line : lines) {
        writer.append(line);
        writer.append("\n");
      }
    }
  }
}
