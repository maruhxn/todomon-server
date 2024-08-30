package com.maruhxn.todomon.domain.item.application;

import com.maruhxn.todomon.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.domain.item.domain.Item;
import com.maruhxn.todomon.domain.item.domain.ItemType;
import com.maruhxn.todomon.domain.item.domain.MoneyType;
import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.member.dto.request.UpsertTitleNameRequest;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.dto.request.ChangePetNameRequest;
import com.maruhxn.todomon.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.domain.purchase.domain.Order;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.maruhxn.todomon.global.common.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Service] - ItemService")
class ItemServiceTest extends IntegrationTestSupport {

    @Autowired
    ItemService itemService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    PetRepository petRepository;

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

        Order order = Order.builder()
                .totalPrice(200L)
                .quantity(2L)
                .moneyType(MoneyType.STARPOINT)
                .merchantUid("test-000000")
                .item(item)
                .build();
        orderRepository.save(order);

        // when
        itemService.postPurchase(member, order);

        // then
        assertThat(inventoryItemRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("member", "item", "quantity")
                .containsExactly(member, item, 2L);

    }

    @Test
    @DisplayName("프리미엄 아이템을 구매 요청 시 유료 플랜을 구독 중이지 않으면 실패한다.")
    void purchaseFAIL() {
        // given
        Member member = createMember();

        Item item = Item.builder()
                .isPremium(true)
                .name(UPSERT_TITLE_NAME_ITEM_NAME)
                .price(100L)
                .itemType(ItemType.CONSUMABLE)
                .effectName("upsertMemberTitleNameEffect")
                .description("칭호 변경권")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        Order order = Order.builder()
                .totalPrice(200L)
                .quantity(2L)
                .moneyType(MoneyType.STARPOINT)
                .merchantUid("test-000000")
                .item(item)
                .build();
        orderRepository.save(order);

        // when / then
        assertThatThrownBy(() -> itemService.postPurchase(member, order))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ErrorCode.NOT_SUBSCRIPTION.getMessage());

    }

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

        Order order = Order.builder()
                .totalPrice(100L)
                .quantity(1L)
                .moneyType(MoneyType.STARPOINT)
                .merchantUid("test-000000")
                .item(item)
                .build();
        orderRepository.save(order);

        // when
        itemService.postPurchase(member, order);

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

        Order order = Order.builder()
                .totalPrice(100L)
                .quantity(1L)
                .moneyType(MoneyType.STARPOINT)
                .merchantUid("test-000000")
                .item(item)
                .build();
        orderRepository.save(order);

        // when
        itemService.postPurchase(member, order);

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

        Order order = Order.builder()
                .totalPrice(100L)
                .quantity(1L)
                .moneyType(MoneyType.STARPOINT)
                .merchantUid("test-000000")
                .item(item)
                .build();
        orderRepository.save(order);

        // when / then
        assertThatThrownBy(() -> itemService.postPurchase(member, order))
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

        Order order = Order.builder()
                .totalPrice(100L)
                .quantity(1L)
                .moneyType(MoneyType.STARPOINT)
                .merchantUid("test-000000")
                .item(item)
                .build();
        orderRepository.save(order);

        // when
        itemService.postPurchase(member, order);

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

        Order order = Order.builder()
                .totalPrice(100L)
                .quantity(1L)
                .moneyType(MoneyType.STARPOINT)
                .merchantUid("test-000000")
                .item(item)
                .build();
        orderRepository.save(order);

        // when
        itemService.postPurchase(member, order);

        // then
        assertThat(member.isSubscribed()).isTrue();
    }

    @Test
    @DisplayName("인벤토리 아이템을 사용한다. [칭호 생성 및 변경권]")
    void useInventoryItem_UPSERT_TITLE_NAME_ITEM_NAME() {
        // given
        Member member = createMember();
        member.updateIsSubscribed(true);

        Item item = Item.builder()
                .isPremium(true)
                .name(UPSERT_TITLE_NAME_ITEM_NAME)
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("upsertMemberTitleNameEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        InventoryItem inventoryItem = InventoryItem.builder()
                .member(member)
                .item(item)
                .quantity(2L)
                .build();
        inventoryItemRepository.save(inventoryItem);

        UpsertTitleNameRequest req = UpsertTitleNameRequest.builder()
                .name("TEST")
                .color("#000000")
                .build();

        // when
        itemService.useInventoryItem(member, UPSERT_TITLE_NAME_ITEM_NAME, req);

        // then
        assertThat(inventoryItem.getQuantity()).isEqualTo(1L);
        assertThat(member.getTitleName())
                .extracting("name", "color")
                .containsExactly("TEST", "#000000");
    }

    @Test
    @DisplayName("인벤토리 아이템을 사용한다. [펫 이름 변경권]")
    void useInventoryItem_CHANGE_PET_NAME_ITEM_NAME() {
        // given
        Member member = createMember();
        member.updateIsSubscribed(true);

        Item item = Item.builder()
                .isPremium(true)
                .name(CHANGE_PET_NAME_ITEM_NAME)
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("changePetNameEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .build();
        itemRepository.save(item);

        InventoryItem inventoryItem = InventoryItem.builder()
                .member(member)
                .item(item)
                .quantity(2L)
                .build();
        inventoryItemRepository.save(inventoryItem);

        Pet pet = Pet.getRandomPet();
        member.addPet(pet);
        petRepository.save(pet);

        ChangePetNameRequest req = ChangePetNameRequest.builder()
                .petId(pet.getId())
                .name("TEST")
                .color("#000000")
                .build();

        // when
        itemService.useInventoryItem(member, CHANGE_PET_NAME_ITEM_NAME, req);

        // then
        assertThat(inventoryItem.getQuantity()).isEqualTo(1L);
        assertThat(pet)
                .extracting("name", "color")
                .containsExactly("TEST", "#000000");
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