import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otp.model.Customer;
import otp.model.CustomerSum;
import otp.model.Payment;
import otp.model.WebShopSum;
import otp.util.CSVReaderUtil;
import otp.util.ReportUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

class CalculusTests {
  private List<Customer> customers;
  private List<Payment> payments;

  @BeforeEach
  void setup() {
    customers = CSVReaderUtil.readCustomers();
    payments = CSVReaderUtil.readPayments(customers);
  }

  @Test
  void testCustomerSums()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    List<CustomerSum> customerSums = (List<CustomerSum>) getCalculateCustomerSumsMethod().invoke(
        null,
        customers, payments);

    assertEquals(7, customerSums.size());
    assertEquals(BigDecimal.valueOf(44299), customerSums.get(0).getSum());
    assertEquals(BigDecimal.valueOf(20234), customerSums.get(1).getSum());
    assertEquals(BigDecimal.ZERO, customerSums.get(2).getSum());
    assertEquals(BigDecimal.valueOf(12000), customerSums.get(3).getSum());
    assertEquals(BigDecimal.valueOf(3186), customerSums.get(4).getSum());
    assertEquals(BigDecimal.valueOf(987), customerSums.get(5).getSum());
    assertEquals(BigDecimal.valueOf(240), customerSums.get(6).getSum());
  }

  @Test
  void testTopCustomers()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    List<CustomerSum> customerSums = (List<CustomerSum>) getCalculateCustomerSumsMethod().invoke(
        null,
        customers, payments);

    List<String> topLines = (List<String>) getTopLinesMethod().invoke(
        null,
        customerSums, 2);

    assertEquals(2, topLines.size());
    assertTrue(topLines.get(0).contains("Kovács János"));
    assertTrue(topLines.get(1).contains("Kiss István"));
  }

  @Test
  void testShopSums()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    List<WebShopSum> webShopSums = (List<WebShopSum>) getCalculateShopSumsMethod().invoke(
        null,
        customers, payments);

    assertEquals(2, webShopSums.size());
    assertEquals("WS01", webShopSums.get(0).getShopId());
    assertEquals(BigDecimal.valueOf(65520), webShopSums.get(0).getCardSum());
    assertEquals(BigDecimal.valueOf(3186), webShopSums.get(0).getTransferSum());
    assertEquals("WS02", webShopSums.get(1).getShopId());
    assertEquals(BigDecimal.valueOf(12240), webShopSums.get(1).getCardSum());
    assertEquals(BigDecimal.ZERO, webShopSums.get(1).getTransferSum());
  }

  private Method getCalculateCustomerSumsMethod() throws NoSuchMethodException {
    Method method = ReportUtil.class.getDeclaredMethod(
        "calculateCustomerSums",
        List.class,
        List.class
    );
    method.setAccessible(true);
    return method;
  }

  private Method getTopLinesMethod() throws NoSuchMethodException {
    Method method = ReportUtil.class.getDeclaredMethod(
        "getTopLines",
        List.class,
        Integer.class
    );
    method.setAccessible(true);
    return method;
  }

  private Method getCalculateShopSumsMethod() throws NoSuchMethodException {
    Method method = ReportUtil.class.getDeclaredMethod(
        "calculateShopSums",
        List.class,
        List.class
    );
    method.setAccessible(true);
    return method;
  }
}
