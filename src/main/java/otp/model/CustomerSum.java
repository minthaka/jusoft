package otp.model;

import java.math.BigDecimal;
import java.util.Objects;

public class CustomerSum {
  private String name;
  private String address;
  private BigDecimal sum;

  public CustomerSum(String name, String address, BigDecimal sum) {
    this.name = name;
    this.address = address;
    this.sum = sum;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public BigDecimal getSum() {
    return sum;
  }

  public String getCSVLine() {
    return this.name + ";" + this.address + ";" + this.sum;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CustomerSum that = (CustomerSum) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(address, that.address) && Objects.equals(sum, that.sum);
  }

  @Override public int hashCode() {
    return Objects.hash(name, address, sum);
  }
}
