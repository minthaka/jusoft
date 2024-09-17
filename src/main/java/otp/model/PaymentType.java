package otp.model;

import java.util.Objects;
import java.util.Optional;

public enum PaymentType {
  CARD("card"),
  TRANSFER("transfer");
  private String value;

  PaymentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Optional<PaymentType> getByValue(String value) {
    if (Objects.isNull(value) || value.length() == 0) {
      return Optional.empty();
    }
    for (PaymentType type : PaymentType.values()) {
      if (type.value.equals(value)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }
}
