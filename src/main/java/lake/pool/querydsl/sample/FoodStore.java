package lake.pool.querydsl.sample;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@ToString(of = {"id", "storeName", "rate", "ownerName"})
public class FoodStore {

    @Id @GeneratedValue
    private Integer id;

    private String storeName;
    private int rate;
    private String ownerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_type_id")
    private FoodType foodType;

    public FoodStore(String storeName, int rate, String ownerName, FoodType foodType) {
        this.storeName = storeName;
        this.rate = rate;
        this.ownerName = ownerName;
        changFoodType(foodType);
    }

    private void changFoodType(FoodType foodType) {
        this.foodType = foodType;
        getFoodType().getFoodStoreList().add(this);
    }
}
