package org.knowm.xchange.okex.v5;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.okex.v5.dto.trade.OkexTradeParams;
import org.knowm.xchange.okex.v5.service.OkexTradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.knowm.xchange.currency.CurrencyPair.TRX_USDT;

@Slf4j
public class OkexExchangeIntegrationTest {
  private static final String API_KEY = "";
  private static final String SECRET_KEY = "";
  private static final String PASSPHRASE = "";

  @Test
  public void testCreateExchangeShouldApplyDefaultSpecification() throws Exception {
    ExchangeSpecification spec =
        ExchangeFactory.INSTANCE
            .createExchange(OkexExchange.class)
            .getDefaultExchangeSpecification();
    final Exchange exchange = ExchangeFactory.INSTANCE.createExchange(spec);

    assertThat(exchange.getExchangeSpecification().getSslUri()).isEqualTo("https://www.okex.com");
    assertThat(exchange.getExchangeSpecification().getHost()).isEqualTo("okex.com");
  }

  @Test
  public void testOrderActions() throws Exception {
    if (!API_KEY.isEmpty() && !SECRET_KEY.isEmpty() && !PASSPHRASE.isEmpty()) {
      ExchangeSpecification spec =
          ExchangeFactory.INSTANCE
              .createExchange(OkexExchange.class)
              .getDefaultExchangeSpecification();
      spec.setApiKey(API_KEY);
      spec.setSecretKey(SECRET_KEY);
      spec.setExchangeSpecificParametersItem("passphrase", PASSPHRASE);

      final Exchange exchange = ExchangeFactory.INSTANCE.createExchange(spec);

      assertThat(exchange.getExchangeSpecification().getSslUri()).isEqualTo("https://www.okex.com");
      assertThat(exchange.getExchangeSpecification().getHost()).isEqualTo("okex.com");

      // Place a single order
      LimitOrder limitOrder =
          new LimitOrder(
              Order.OrderType.ASK, BigDecimal.TEN, TRX_USDT, null, new Date(), new BigDecimal(100));

      String orderId = exchange.getTradeService().placeLimitOrder(limitOrder);
      log.info("Placed orderId: {}", orderId);

      // Amend the above order
      LimitOrder limitOrder2 =
          new LimitOrder(
              Order.OrderType.ASK,
              BigDecimal.TEN,
              TRX_USDT,
              orderId,
              new Date(),
              new BigDecimal(1000));
      String orderId2 = exchange.getTradeService().changeOrder(limitOrder2);
      log.info("Amended orderId: {}", orderId2);

      // Cancel that order
      boolean result =
          exchange
              .getTradeService()
              .cancelOrder(new OkexTradeParams.OkexCancelOrderParams(TRX_USDT, orderId2));
      log.info("Cancellation result: {}", result);

      // Place batch orders
      List<String> orderIds =
          ((OkexTradeService) exchange.getTradeService())
              .placeLimitOrder(Arrays.asList(limitOrder, limitOrder, limitOrder));
      log.info("Placed batch orderIds: {}", orderIds);

      // Amend batch orders
      List<LimitOrder> amendOrders = new ArrayList<>();
      for (String id : orderIds) {
        amendOrders.add(
            new LimitOrder(
                Order.OrderType.ASK,
                BigDecimal.TEN,
                TRX_USDT,
                id,
                new Date(),
                new BigDecimal(1000)));
      }
      List<String> amendedOrderIds =
          ((OkexTradeService) exchange.getTradeService()).changeOrder(amendOrders);
      log.info("Amended batch orderIds: {}", amendedOrderIds);

      OpenOrders openOrders = ((OkexTradeService) exchange.getTradeService()).getOpenOrders();
      log.info("Open Orders: {}", openOrders);

      // Cancel batch orders
      List<CancelOrderParams> cancelOrderParams = new ArrayList<>();
      for (String id : orderIds) {
        cancelOrderParams.add(new OkexTradeParams.OkexCancelOrderParams(TRX_USDT, id));
      }
      List<Boolean> results =
          ((OkexTradeService) exchange.getTradeService()).cancelOrder(cancelOrderParams);
      log.info("Cancelled order results: {}", results);
    }
  }
}
