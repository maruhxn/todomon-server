package com.maruhxn.todomon.core.domain.payment.application;

import com.maruhxn.todomon.core.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.ItemType;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.item.implement.ItemReader;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.payment.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.payment.dao.TodomonPaymentRepository;
import com.maruhxn.todomon.core.domain.payment.domain.Order;
import com.maruhxn.todomon.core.domain.payment.domain.OrderStatus;
import com.maruhxn.todomon.core.domain.payment.domain.PaymentStatus;
import com.maruhxn.todomon.core.domain.payment.domain.TodomonPayment;
import com.maruhxn.todomon.core.domain.payment.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.domain.payment.dto.request.WebhookPayload;
import com.maruhxn.todomon.core.domain.payment.implement.*;
import com.maruhxn.todomon.core.domain.purchase.implement.PurchaseManager;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import com.maruhxn.todomon.core.util.IntegrationTestSupport;
import com.maruhxn.todomon.infra.payment.error.InvalidPaymentAmountException;
import com.maruhxn.todomon.infra.payment.error.PrepareException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("[Service] - PaymentService")
class PaymentServiceTest extends IntegrationTestSupport {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberReader memberReader;

    @Autowired
    private ItemReader itemReader;

    @Autowired
    private OrderReader orderReader;

    @Autowired
    private OrderWriter orderWriter;

    @Autowired
    private RollbackManager rollbackManager;

    @Autowired
    private RefundProvider refundProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TodomonPaymentRepository todomonPaymentRepository;

    @MockBean
    private PurchaseManager purchaseManager;

    @MockBean
    private PaidOrderProducer paidOrderProducer;

    private Member member;
    private Item item;
    private String merchantUid = "merchantUid123";

    @BeforeEach
    void setUp() {
        member = this.createMember("tester");
        saveMemberToContext(member);
        item = Item.builder()
                .isPremium(false)
                .name("유료 플랜 구독권")
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("subscribeEffect")
                .description("구독권")
                .moneyType(MoneyType.REAL_MONEY)
                .build();
        itemRepository.save(item);
    }

    @Test
    @DisplayName("사전 검증 성공")
    void preparePayment_Success() throws IOException {
        // given
        PreparePaymentReq req = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        willDoNothing().given(paymentProvider).prepare(anyString(), any());

        // when
        paymentService.preparePayment(member.getId(), req);

        // then
        verify(paymentProvider, times(1)).prepare(merchantUid, req.getAmount());
        Order order = orderReader.findByMerchantUid(merchantUid);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REQUESTED);
    }

    @Test
    @DisplayName("사전 검증 실패")
    void preparePayment_Failure() throws IOException {
        // given
        PreparePaymentReq req = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        willThrow(new PrepareException()).given(paymentProvider).prepare(merchantUid, req.getAmount());

        // when
        assertThatThrownBy(() -> paymentService.preparePayment(member.getId(), req))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("결제 금액이 일치하지 않습니다.");

        // then
        verify(paymentProvider, times(1)).prepare(merchantUid, req.getAmount());
        Order order = orderReader.findByMerchantUid(merchantUid);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("사전 검증 실패 by 타임아웃")
    void preparePayment_FailureWithTimeout() throws IOException {
        // given
        PreparePaymentReq req = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        String timeoutErrMsg = "타임아웃 에러 발생";
        willThrow(new IOException(timeoutErrMsg)).given(paymentProvider).prepare(merchantUid, req.getAmount());

        // when
        assertThatThrownBy(() -> paymentService.preparePayment(member.getId(), req))
                .isInstanceOf(InternalServerException.class)
                .hasMessage(timeoutErrMsg);

        // then
        then(paymentProvider).should(times(1)).prepare(merchantUid, req.getAmount());
        Order order = orderReader.findByMerchantUid(merchantUid);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("사후 검증 성공")
    void completePayment_Success() throws IOException {
        // given
        PreparePaymentReq preparePaymentReq = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        orderWriter.create(item, member, preparePaymentReq);

        String impUid = "impUid123";
        WebhookPayload req = WebhookPayload.builder()
                .merchant_uid(merchantUid)
                .imp_uid(impUid)
                .status("paid")
                .cancellation_id("")
                .build();

        willDoNothing().given(paidOrderProducer).send(member.getId(), merchantUid);
        willDoNothing().given(paymentProvider).complete(impUid, preparePaymentReq.getAmount());

        // when
        paymentService.completePayment(req);

        // then
        then(paymentProvider).should(times(1)).complete(impUid, preparePaymentReq.getAmount());
        Order order = orderReader.findByMerchantUid(merchantUid);
        Optional<TodomonPayment> optionalTodomonPayment = todomonPaymentRepository.findById(impUid);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(optionalTodomonPayment).isNotEmpty();
        assertThat(optionalTodomonPayment.get().getStatus()).isEqualTo(PaymentStatus.OK);
    }

    @Test
    @DisplayName("사후 검증 실패")
    void completePayment_Failure() throws IOException {
        // given
        PreparePaymentReq preparePaymentReq = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        orderWriter.create(item, member, preparePaymentReq);

        String impUid = "impUid123";
        WebhookPayload req = WebhookPayload.builder()
                .merchant_uid(merchantUid)
                .imp_uid(impUid)
                .status("paid")
                .cancellation_id("")
                .build();

        willDoNothing().given(paidOrderProducer).send(member.getId(), merchantUid);
        InvalidPaymentAmountException invalidPaymentAmountException = new InvalidPaymentAmountException();
        willThrow(invalidPaymentAmountException).given(paymentProvider).complete(any(), any());

        // when
        assertThatThrownBy(() -> paymentService.completePayment(req))
                .isInstanceOf(InternalServerException.class)
                .hasMessage(invalidPaymentAmountException.getMessage());

        // then
        then(paymentProvider).should(times(1)).complete(impUid, preparePaymentReq.getAmount());
        Order order = orderReader.findByMerchantUid(merchantUid);
        Optional<TodomonPayment> optionalTodomonPayment = todomonPaymentRepository.findById(impUid);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(optionalTodomonPayment).isNotEmpty();
        assertThat(optionalTodomonPayment.get().getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("사후 검증 실패 + 환불 실패")
    void completePayment_Failure_And_Refund_Failure() throws IOException {
        // given
        PreparePaymentReq preparePaymentReq = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        Order order = orderWriter.create(item, member, preparePaymentReq);

        String impUid = "impUid123";
        WebhookPayload req = WebhookPayload.builder()
                .merchant_uid(merchantUid)
                .imp_uid(impUid)
                .status("paid")
                .cancellation_id("")
                .build();

        willDoNothing().given(paidOrderProducer).send(member.getId(), merchantUid);
        InvalidPaymentAmountException invalidPaymentAmountException = new InvalidPaymentAmountException();
        willThrow(invalidPaymentAmountException).given(paymentProvider).complete(any(), any());
        willThrow(new IOException("환불 중 에러 발생")).given(paymentProvider).refund(impUid);

        // when
        assertThatThrownBy(() -> paymentService.completePayment(req))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("환불 중 에러 발생");

        // then
        then(paymentProvider).should(times(1)).complete(impUid, preparePaymentReq.getAmount());
        then(paymentProvider).should(times(1)).refund(impUid);
        then(mailService).should(times(1)).sendEmail(member.getEmail(), "환불에 실패했습니다. 관리자에게 문의바랍니다.");
        Optional<TodomonPayment> optionalTodomonPayment = todomonPaymentRepository.findById(impUid);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(optionalTodomonPayment).isNotEmpty();
        assertThat(optionalTodomonPayment.get().getStatus()).isEqualTo(PaymentStatus.REFUND_FAILED);
    }

    @Test
    @DisplayName("아이템 구매 성공")
    void purchaseItem_Success() throws IOException {
        // given
        PreparePaymentReq preparePaymentReq = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        Order order = orderWriter.create(item, member, preparePaymentReq);
        String impUid = "impUid123";
        TodomonPayment todomonPayment = todomonPaymentRepository.save(TodomonPayment.of(order, impUid));
        order.setPayment(todomonPayment);
        order.updateStatus(OrderStatus.PAID);

        willDoNothing().given(purchaseManager).purchase(member, item, 1L);

        // when
        paymentService.purchaseItem(member.getId(), merchantUid);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.OK);
    }

    @Test
    @DisplayName("아이템 구매 실패")
    void purchaseItem_Failure() throws IOException {
        // given
        PreparePaymentReq preparePaymentReq = PreparePaymentReq.builder()
                .merchant_uid(merchantUid)
                .itemId(item.getId())
                .quantity(1L)
                .amount(BigDecimal.valueOf(item.getPrice() * 1L))
                .build();

        Order order = orderWriter.create(item, member, preparePaymentReq);
        String impUid = "impUid123";
        TodomonPayment todomonPayment = todomonPaymentRepository.save(TodomonPayment.of(order, impUid));
        order.setPayment(todomonPayment);
        order.updateStatus(OrderStatus.PAID);

        willThrow(new RuntimeException("알 수 없는 에러 발생")).given(purchaseManager).purchase(member, item, 1L);

        // when
        assertThatThrownBy(() -> paymentService.purchaseItem(member.getId(), merchantUid))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("알 수 없는 에러 발생");

        // then
        then(paymentProvider).should(times(1)).refund(impUid);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("환불 성공")
    void cancelPayment_Success() throws IOException {
        // given
        Order order = Order.builder()
                .item(item)
                .member(member)
                .totalPrice(item.getPrice() * 1L)
                .quantity(1L)
                .merchantUid(merchantUid)
                .moneyType(MoneyType.REAL_MONEY)
                .build();
        order.updateStatus(OrderStatus.OK);

        String impUid = "impUid123";

        TodomonPayment todomonPayment = todomonPaymentRepository.save(TodomonPayment.of(order, impUid));
        order.setPayment(todomonPayment);
        orderRepository.save(order);

        willDoNothing().given(paymentProvider).refund(impUid);

        // when
        paymentService.cancelPayment(member.getId(), merchantUid);

        // then
        then(paymentProvider).should(times(1)).refund(impUid);
        Order findOrder = orderRepository.findByMerchantUid(merchantUid).get();
        assertThat(findOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(todomonPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("환불 실패")
    void cancelPayment_Failure() throws IOException {
        // given
        Order order = Order.builder()
                .item(item)
                .member(member)
                .totalPrice(item.getPrice() * 1L)
                .quantity(1L)
                .merchantUid(merchantUid)
                .moneyType(MoneyType.REAL_MONEY)
                .build();
        order.updateStatus(OrderStatus.OK);

        String impUid = "impUid123";

        TodomonPayment todomonPayment = todomonPaymentRepository.save(TodomonPayment.of(order, impUid));
        order.setPayment(todomonPayment);
        orderRepository.save(order);

        willThrow(new IOException("환불 시도 중 에러 발생")).given(paymentProvider).refund(impUid);

        // when
        assertThatThrownBy(() -> paymentService.cancelPayment(member.getId(), merchantUid))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("환불 시도 중 에러 발생");

        // then
        then(paymentProvider).should(times(1)).refund(impUid);
        then(mailService).should(times(1)).sendEmail(member.getEmail(), "환불에 실패했습니다. 관리자에게 문의바랍니다.");
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.OK);
        assertThat(todomonPayment.getStatus()).isEqualTo(PaymentStatus.REFUND_FAILED);
    }

    private Member createMember(String username) {
        Member member = Member.builder()
                .username(username)
                .email(username + "@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_" + username)
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return memberRepository.save(member);
    }

}