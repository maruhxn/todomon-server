package com.maruhxn.todomon.core.domain.item.application;

import com.maruhxn.todomon.core.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.core.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.ItemType;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.dto.request.ChangePetNameReq;
import com.maruhxn.todomon.core.domain.payment.dao.OrderRepository;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.maruhxn.todomon.core.global.common.Constants.CHANGE_PET_NAME_ITEM_NAME;
import static com.maruhxn.todomon.core.global.common.Constants.UPSERT_TITLE_NAME_ITEM_NAME;
import static org.assertj.core.api.Assertions.assertThat;

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

        UpsertTitleNameReq req = UpsertTitleNameReq.builder()
                .name("TEST")
                .color("#000000")
                .build();

        // when
        itemService.useInventoryItem(member.getId(), UPSERT_TITLE_NAME_ITEM_NAME, req);

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

        ChangePetNameReq req = ChangePetNameReq.builder()
                .petId(pet.getId())
                .name("TEST")
                .color("#000000")
                .build();

        // when
        itemService.useInventoryItem(member.getId(), CHANGE_PET_NAME_ITEM_NAME, req);

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