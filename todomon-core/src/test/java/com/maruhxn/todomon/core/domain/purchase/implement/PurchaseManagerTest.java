package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.core.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.ItemType;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.maruhxn.todomon.core.global.common.Constants.MAX_PET_HOUSE_SIZE;
import static com.maruhxn.todomon.core.global.common.Constants.UPSERT_TITLE_NAME_ITEM_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Implement] - PurchaseManager")
class PurchaseManagerTest extends IntegrationTestSupport {

    @Autowired
    PurchaseManager purchaseManager;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    InventoryItemRepository inventoryItemRepository;

    @Test
    @DisplayName("CONSUMABLE 아이템을 구매하면 인벤토리에 저장된다.")
    void purchase() {
        // given
        Member member = createMember();

        Item item = Item.builder()
                .name(UPSERT_TITLE_NAME_ITEM_NAME)
                .price(100L)
                .itemType(ItemType.CONSUMABLE)
                .effectName("upsertMemberTitleNameEffect")
                .description("칭호 변경권")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        // when
        purchaseManager.purchase(member, item, 2L);

        // then
        assertThat(inventoryItemRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("member", "item", "quantity")
                .containsExactly(member, item, 2L);

    }

//    @Test
//    @DisplayName("프리미엄 아이템을 구매 요청 시 유료 플랜을 구독 중이지 않으면 실패한다.")
//    void purchaseFAIL() {
//        // given
//        Member member = createMember();
//
//        Item item = Item.builder()
//                .isPremium(true)
//                .name(UPSERT_TITLE_NAME_ITEM_NAME)
//                .price(100L)
//                .itemType(ItemType.CONSUMABLE)
//                .effectName("upsertMemberTitleNameEffect")
//                .description("칭호 변경권")
//                .moneyType(MoneyType.STARPOINT)
//                .build();
//        itemRepository.save(item);
//
//        Order order = Order.builder()
//                .totalPrice(200L)
//                .quantity(2L)
//                .moneyType(MoneyType.STARPOINT)
//                .member(member)
//                .merchantUid("test-000000")
//                .item(item)
//                .build();
//        orderRepository.save(order);
//
//        // when / then
//        assertThatThrownBy(() -> purchaseManager.purchase(order))
//                .isInstanceOf(ForbiddenException.class)
//                .hasMessage(ErrorCode.NOT_SUBSCRIPTION.getMessage());
//
//    }

    @Test
    @DisplayName("펫 소환 아이템을 구매하면 펫을 소환한다.")
    void purchasePetSummonItem() {
        // given
        Member member = createMember();

        Item item = Item.builder()
                .name("test")
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("petSummonEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        // when
        purchaseManager.purchase(member, item, 1L);

        // then
        assertThat(member.getPets().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("펫 하우스 확장 아이템을 구매하면 즉시 한 칸 증가된다.")
    void purchasePetHouseExpansionItem() {
        // given
        Member member = createMember();

        Item item = Item.builder()
                .name("test")
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("petHouseExpansionEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        // when
        purchaseManager.purchase(member, item, 1L);

        // then
        assertThat(member.getPetHouseSize()).isEqualTo(4);
    }

    @Test
    @DisplayName("펫 하우스 확장 아이템을 구매했을 때, 유저의 펫 하우스 크기가 최대 크기 이상이었을 경우 실패한다.")
    void purchasePetHouseExpansionItemFail() {
        // given
        Member member = createMember();
        for (int i = 0; i < MAX_PET_HOUSE_SIZE - 3; i++) {
            member.expandPetHouse();
        }

        Item item = Item.builder()
                .name("test")
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("petHouseExpansionEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        // when / then
        assertThatThrownBy(() -> purchaseManager.purchase(member, item, 1L))
                .hasMessage(String.format("펫 하우스는 %d칸을 초과할 수 없습니다.", MAX_PET_HOUSE_SIZE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("당근 아이템을 구매하면 당근이 증가한다.")
    void purchasePlusCarrotItem() {
        // given
        Member member = createMember();

        Item item = Item.builder()
                .name("test")
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("plusCarrotEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        // when
        purchaseManager.purchase(member, item, 1L);

        // then
        assertThat(member.getFoodCnt()).isEqualTo(10);
    }

    @Test
    @DisplayName("유료 플랜을 구매하면 유저의 구독 여부가 true가 된다.")
    void purchaseSubscribeItem() {
        // given
        Member member = createMember();

        Item item = Item.builder()
                .name("test")
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("subscribeEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        // when
        purchaseManager.purchase(member, item, 1L);

        // then
        assertThat(member.isSubscribed()).isTrue();
    }

    private Member createMember() {
        Member member = Member.builder()
                .username("tester")
                .email("test@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_foobarfoobar")
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return memberRepository.save(member);
    }
}