package lake.pool.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lake.pool.querydsl.sample.*;
import lake.pool.querydsl.sample.food.FoodStoreDTO;
import lake.pool.querydsl.sample.food.QFoodStoreDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static lake.pool.querydsl.sample.QFoodStore.foodStore;
import static lake.pool.querydsl.sample.QFoodType.foodType;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Transactional
public class QueryDSLTest {

    @Autowired
    FoodTypeRepository foodTypeRepository;

    @Autowired
    FoodStoreRepository foodStoreRepository;

    @Autowired
    JPAQueryFactory query;

    @Test
    public void setData(){
        FoodType korean = new FoodType("한식", 1);
        FoodType western = new FoodType("양식", 2);
        FoodType chinese = new FoodType("중식", 3);

        foodTypeRepository.saveAll(List.of(korean,western,chinese));

        FoodStore foodStore1 = new FoodStore("삼겹살", 9, "sangmessi", korean);
        FoodStore foodStore2 = new FoodStore("닭갈비", 2, "sangmessi", korean);
        FoodStore foodStore3 = new FoodStore("부대찌개", 3, "lake", korean);
        FoodStore foodStore4 = new FoodStore("순대국밥", 4, "lake", korean);
        FoodStore foodStore5 = new FoodStore("소고기", 5, "lake", korean);
        FoodStore foodStore6 = new FoodStore("스파게티", 6, "sangmessi", western);
        FoodStore foodStore7 = new FoodStore("피자", 7, "sangmessi", western);
        FoodStore foodStore8 = new FoodStore("중국집", 8, "hong", chinese);
        FoodStore foodStore9 = new FoodStore("중국집2", 9, "hong", chinese);
        FoodStore foodStore10 = new FoodStore("중국집3", 10, "hong", chinese);

        foodStoreRepository.saveAll(List.of(foodStore1,foodStore2,foodStore3,foodStore4,foodStore5,foodStore6,foodStore7,foodStore8,foodStore9,foodStore10));
    }

    @Test
    public void 기본쿼리(){
        List<FoodStore> results = query
                .selectFrom(foodStore)
                .fetch();

        assertThat(results.size()).isEqualTo(10);
    }

    @Test
    public void 기본쿼리_조건절(){
        List<FoodStore> results = query.selectFrom(foodStore)
                .where(foodStore.rate.goe(5))
                .fetch();

        assertThat(results.size()).isEqualTo(7);
    }

    @Test
    public void 기본쿼리_조건절2(){
        List<FoodStore> results = query.selectFrom(foodStore)
                .where(
                        foodStore.rate.goe(5),
                        foodStore.storeName.startsWith("삼")
                        )
                .fetch();

        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void 기본쿼리_정렬(){
        List<FoodStore> results = query.selectFrom(foodStore)
                .orderBy(foodStore.rate.desc())
                .fetch();

        assertThat(results.size()).isEqualTo(10);
        assertThat(results.get(0).getRate()).isEqualTo(10);
    }

    @Test
    public void 기본쿼리_페이징(){
        QueryResults<FoodStore> fetchResults = query.selectFrom(foodStore)
                .offset(0)
                .limit(3)
                .fetchResults();

        List<FoodStore> results = fetchResults.getResults();
        long limit = fetchResults.getLimit();
        long offset = fetchResults.getOffset();
        long total = fetchResults.getTotal();

        System.out.println("results = " + results);
        System.out.println("limit = " + limit);
        System.out.println("offset = " + offset);
        System.out.println("total = " + total);

        assertThat(results.size()).isEqualTo(3);
    }

    @Test
    public void join(){ //inner join
        List<FoodStore> fetch = query.selectFrom(foodStore)
                .join(foodStore.foodType, foodType)
                .fetch();

        fetch.forEach(System.out::println);
    }

    @Test
    public void 연관관계_없는_조인(){ //cross join
        List<FoodStore> results = query.select(foodStore)
                .from(foodStore, foodType)
                .where(foodStore.rate.eq(foodType.foodOrder))
                .fetch();

        assertThat(results.size()).isEqualTo(2);
    }

    @Test
    public void join_on(){  // inner join
        List<FoodStore> results = query.select(foodStore)
                .from(foodStore)
                .join(foodType).on(foodType.foodOrder.eq(foodStore.rate))
                .fetch();

        assertThat(results.size()).isEqualTo(2);
    }

    @Test
    public void left_join(){  // left join
        List<Tuple> results = query.select(foodStore, foodType)
                .from(foodStore)
                .leftJoin(foodType).on(foodType.foodOrder.eq(foodStore.rate))
                .fetch();

        results.forEach(System.out::println);
//        Tuple tuple = results.get(0);
//        Integer integer = tuple.get(foodStore.rate);
        assertThat(results.size()).isEqualTo(10);
    }

    @Test
    public void subQuery(){
        List<FoodStore> results = query.selectFrom(foodStore)
                .where(foodStore.rate.in(
                        JPAExpressions
                                .select(foodType.foodOrder.max())
                                .from(foodType)
                        )
                )
                .fetch();

        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void caseQuery(){
        List<String> results = query.select(
                foodStore.rate
                        .when(10).then("존맛탱")
                        .when(9).then("맛남")
                        .otherwise("그럭저럭"))
                .from(foodStore)
                .fetch();

        System.out.println("results = " + results);

        List<String> results2 = query
                .select(new CaseBuilder()
                        .when(foodStore.rate.goe(7)).then("존맛탱")
                        .when(foodStore.rate.goe(4)).then("맛남")
                        .otherwise("그럭저럭"))
                .from(foodStore)
                .orderBy(foodStore.rate.desc())
                .fetch();

        System.out.println("results2 = " + results2);
    }

    @Test
    public void 내가_원하는_객체리턴(){
        List<FoodStoreDTO> foodStoreDTOList = query.select(new QFoodStoreDTO(
                foodStore.storeName,
                foodStore.rate,
                foodStore.ownerName,
                foodType.foodTypeName,
                foodType.foodOrder))
                .from(foodStore)
                .join(foodStore.foodType, foodType)
                .fetch();

        foodStoreDTOList.forEach(System.out::println);
    }
}
