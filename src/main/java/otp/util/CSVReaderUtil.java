package otp.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import otp.model.Customer;
import otp.model.Payment;
import otp.model.PaymentType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CSVReaderUtil {
  private static final Logger log = LogManager.getLogger(CSVReaderUtil.class);

  private CSVReaderUtil() {
    throw new UnsupportedOperationException("This is a util class!");
  }

  public static List<Customer> readCustomers() {
    List<String> customerLines = getContent("src/main/resources/csv/customer.csv");
    return getCustomers(customerLines);
  }

  private static List<Customer> getCustomers(List<String> customerLines) {
    List<Customer> customers = new ArrayList<>();
    Map<String, List<String>> customersByShop = new HashMap<>();
    for (String line : customerLines) {
      if (validCustomerData(line, customersByShop)) {
        customers.add(createCustomer(line));
      }
    }
    return customers;
  }

  private static Customer createCustomer(String line) {
    String[] parts = line.split(";");
    Customer customer = new Customer();
    customer.setShopId(parts[0]);
    customer.setCustomerId(parts[1]);
    customer.setName(parts[2]);
    customer.setAddress(parts[3]);
    return customer;
  }

  private static boolean validCustomerData(String line, Map<String, List<String>> customersByShop) {
    if (Objects.isNull(line)) {
      log.error("The provided line of customer is null");
      return false;
    }
    String[] parts = line.split(";");
    if (parts.length != 4) {
      log.error(
          "The provided line of customer data doesn't contain the required number of fields (4): " +
              "{}",
          line);
      return false;
    }
    if (parts[0].isBlank()) {
      log.error("The shop identifier must not be empty");
      return false;
    }
    if (parts[1].isBlank()) {
      log.error("The customer identifier must not be empty");
      return false;
    }
    if (parts[2].isBlank()) {
      log.error("The customer name must not be empty");
      return false;
    }
    if (parts[3].isBlank()) {
      log.error("The customer address must not be empty");
      return false;
    }

    if (!parts[0].equals("WS01") && !parts[0].equals("WS02")) {
      log.error("The shop identifier is none of the allowed ones: {}", line);
      return false;
    }

    if (Objects.isNull(customersByShop.get(parts[0]))) {
      customersByShop.put(parts[0], new ArrayList<>());
    }

    if (customersByShop.get(parts[0]).contains(parts[1])) {
      log.error("The customer id {} for the given shop id {} is not unique!", parts[1], parts[0]);
      return false;
    } else {
      customersByShop.get(parts[0]).add(parts[1]);
    }

    return true;
  }

  public static List<Payment> readPayments(List<Customer> customers) {
    List<String> paymentLines = getContent("src/main/resources/csv/payments.csv");
    List<Payment> payments = new ArrayList<>();
    for (String line : paymentLines) {
      if (validPaymentData(line, customers)) {
        payments.add(createPayment(line));
      }
    }
    return payments;
  }

  private static Payment createPayment(String line) {
    String[] parts = line.split(";");
    Payment payment = new Payment();
    payment.setShopId(parts[0]);
    payment.setCustomerId(parts[1]);
    payment.setType((PaymentType.getByValue(parts[2]).get()));
    payment.setAmount(new BigDecimal(parts[3]));
    payment.setBankAccount(parts[4]);
    payment.setCardNumber(parts[5]);
    payment.setPaymentDate(LocalDate.parse(parts[6], DateTimeFormatter.ofPattern("yyyy.MM.dd")));
    return payment;
  }

  private static boolean validPaymentData(String line, List<Customer> customers) {
    if (Objects.isNull(line)) {
      log.error("The provided line of payments is null");
      return false;
    }
    String[] parts = line.split(";");
    if (parts.length != 7) {
      log.error(
          "The provided line of payment data doesn't contain the required number of fields (7): {}",
          line);
      return false;
    }
    if (parts[0].isBlank()) {
      log.error("The shop identifier must not be empty");
      return false;
    }
    if (parts[1].isBlank()) {
      log.error("The customer identifier must not be empty");
      return false;
    }
    if (parts[2].isBlank()) {
      log.error("The payment method must not be empty");
      return false;
    }
    if (parts[3].isBlank()) {
      log.error("The paid amount must not be empty");
      return false;
    }
    if (parts[6].isBlank()) {
      log.error("The date of transaction must not be empty");
      return false;
    }
    if (invalidCustomer(parts[0], parts[1], customers)) {
      log.error("The provided customer doesn't exist with shop id = {} and customer id = {}",
          parts[0], parts[1]);
      return false;
    }
    Optional<PaymentType> paymentTypeFromInput = PaymentType.getByValue(parts[2]);
    if (paymentTypeFromInput.isEmpty()) {
      log.error("The provided payment type is invalid: {}", parts[2]);
      return false;
    }
    PaymentType paymentType = paymentTypeFromInput.get();
    if (paymentType == PaymentType.CARD && parts[5].isBlank()) {
      log.error("No card number present for a payment by card.");
      return false;
    }
    if (paymentType == PaymentType.TRANSFER && parts[4].isBlank()) {
      log.error("No account number present for a payment by transfer.");
      return false;
    }
    try {
      new BigDecimal(parts[3]);
    } catch (NumberFormatException e) {
      log.error("The provided amount of the transaction is invalid: {}", parts[3]);
      return false;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    try {
      LocalDate.parse(parts[6], formatter);
    } catch (DateTimeParseException e) {
      log.error("The provided transaction date of the transaction is invalid: {}", parts[6]);
      return false;
    }
    return true;
  }

  private static boolean invalidCustomer(String shopId, String customerId,
      List<Customer> customers) {
    for (Customer customer : customers) {
      if (customer.getShopId().equals(shopId) && customer.getCustomerId().equals(customerId)) {
        return false;
      }
    }
    return true;
  }

  private static List<String> getContent(String fileName) {
    List<String> content = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.add(line);
      }
    } catch (IOException e) {
      log.error("Error reading file {}: {}", fileName, e.getMessage());
      return List.of();
    }
    return content;
  }
}
