package lake.pool.querydsl.sample.food;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public class FoodStoreDTO {

    private String storeName;
    private int rate;
    private String ownerName;
    private String foodTypeName;
    private int foodOrder;

    @QueryProjection
    public FoodStoreDTO(String storeName, int rate, String ownerName, String foodTypeName, int foodOrder) {
        this.storeName = storeName;
        this.rate = rate;
        this.ownerName = ownerName;
        this.foodTypeName = foodTypeName;
        this.foodOrder = foodOrder;
    }
}
