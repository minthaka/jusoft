package otp.model;

import java.math.BigDecimal;

public class WebShopSum {
  private String shopId;
  private BigDecimal cardSum;
  private BigDecimal transferSum;

  public WebShopSum(String shopId, BigDecimal cardSum, BigDecimal transferSum) {
    this.shopId = shopId;
    this.cardSum = cardSum;
    this.transferSum = transferSum;
  }

  public String getShopId() {
    return shopId;
  }

  public BigDecimal getCardSum() {
    return cardSum;
  }

  public BigDecimal getTransferSum() {
    return transferSum;
  }

  public String getCSVLine() {
    return this.shopId + ";" + this.cardSum + ";" + this.transferSum;
  }
}
