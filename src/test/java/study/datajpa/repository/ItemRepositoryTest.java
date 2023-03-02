package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Item;

/**
 * 새로운 엔티티 구별하는 방법
 * - 새로운 엔티티면 저장(persist)
 * - 새로운 엔티티가 아니면 병합(merge)
 * 식별자가 객체일 때 null로 판단... ex. Long id
 * 식별자가 자바 기본 타입일 때 0으로 판단... ex. long id
 */
@SpringBootTest
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Test
    public void save() {
        Item item = new Item("A");
        itemRepository.save(item);

    }
}