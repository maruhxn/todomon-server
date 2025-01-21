package com.maruhxn.todomon.core.domain.item.api;

import com.maruhxn.todomon.core.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.core.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.ItemType;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.dto.request.ChangePetNameReq;
import com.maruhxn.todomon.core.domain.payment.dao.OrderRepository;
import com.maruhxn.todomon.core.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static com.maruhxn.todomon.core.global.auth.implement.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.core.global.common.Constants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Item")
class ItemIntegrationTest extends ControllerIntegrationTestSupport {

    static final String ITEM_BASE_URL = "/api/items";

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
    @DisplayName("[POST] - /api/items/use?itemName=펫 이름 변경권")
    void useInventoryItem_CHANGE_PET_NAME_ITEM_NAME() throws Exception {
        // given
        member.updateIsSubscribed(true);

        Item item = Item.builder()
                .name(CHANGE_PET_NAME_ITEM_NAME)
                .price(100L)
                .itemType(ItemType.IMMEDIATE_EFFECT)
                .effectName("changePetNameEffect")
                .description("test")
                .moneyType(MoneyType.STARPOINT)
                .isPremium(true)
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

        // when / then
        mockMvc.perform(
                        post(ITEM_BASE_URL + "/use")
                                .queryParam("itemName", CHANGE_PET_NAME_ITEM_NAME)
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

//    @Test
//    @DisplayName("[POST] - /api/items/use?itemName=펫 이름 변경권 요청 시, 프리미엄 아이템일 경우 구독 중이지 않으면 403 에러를 반환한다.")
//    void useInventoryItemFailWhenIsPremiumButNotSubscribing() throws Exception {
//        // given
//        Item item = Item.builder()
//                .name(CHANGE_PET_NAME_ITEM_NAME)
//                .price(100L)
//                .itemType(ItemType.IMMEDIATE_EFFECT)
//                .effectName("changePetNameEffect")
//                .description("test")
//                .moneyType(MoneyType.STARPOINT)
//                .isPremium(true)
//                .build();
//        itemRepository.save(item);
//
//        InventoryItem inventoryItem = InventoryItem.builder()
//                .member(member)
//                .item(item)
//                .quantity(2L)
//                .build();
//        inventoryItemRepository.save(inventoryItem);
//
//        Pet pet = Pet.getRandomPet();
//        member.addPet(pet);
//        petRepository.save(pet);
//
//        ChangePetNameReq req = ChangePetNameReq.builder()
//                .petId(pet.getId())
//                .name("TEST")
//                .color("#000000")
//                .build();
//
//        // when / then
//        mockMvc.perform(
//                        post(ITEM_BASE_URL + "/use")
//                                .queryParam("itemName", CHANGE_PET_NAME_ITEM_NAME)
//                                .content(objectMapper.writeValueAsString(req))
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
//                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
//                )
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("code").value(ErrorCode.NOT_SUBSCRIPTION.name()))
//                .andExpect(jsonPath("message").value(ErrorCode.NOT_SUBSCRIPTION.getMessage()));
//    }

    @Test
    @DisplayName("[POST] - /api/items/use?itemName=칭호 변경권")
    void useInventoryItem_UPSERT_TITLE_NAME_ITEM_NAME() throws Exception {
        // given
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

        // when / then
        mockMvc.perform(
                        post(ITEM_BASE_URL + "/use")
                                .queryParam("itemName", UPSERT_TITLE_NAME_ITEM_NAME)
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

}