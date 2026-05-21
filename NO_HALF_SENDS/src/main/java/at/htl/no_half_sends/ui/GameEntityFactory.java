package at.htl.no_half_sends.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;

public class GameEntityFactory implements EntityFactory {

    @Spawns("BORDER")
    public Entity newBorder(SpawnData data) {
        // Sicherer Cast über Number löst das ClassCastException-Problem vollständig!
        double width = ((Number) data.get("width")).doubleValue();
        double height = ((Number) data.get("height")).doubleValue();

        return FXGL.entityBuilder(data)
                .type(GameType.BORDER)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(new CollidableComponent(true))
                .build();
    }
}