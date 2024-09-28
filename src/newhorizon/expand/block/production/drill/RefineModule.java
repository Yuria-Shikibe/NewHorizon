package newhorizon.expand.block.production.drill;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;

import static mindustry.type.ItemStack.with;

public class RefineModule extends DrillModule{
    public Color flameColor = Color.valueOf("f58349"), midColor = Color.valueOf("f2d585");
    public float flameRad = 1f, circleSpace = 2f, flameRadiusScl = 8f, flameRadiusMag = 0.6f, circleStroke = 1.5f;

    public float alpha = 0.5f;
    public int particles = 12;
    public float particleLife = 70f, particleRad = 7f, particleSize = 3f, fadeMargin = 0.4f, rotateScl = 1.5f;
    public Interp particleInterp = new Interp.PowIn(1.5f);

    public RefineModule() {
        super("refine-module");
        requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
        size = 2;
        boostFinalMul = -0.25f;
        powerMul = 1.2f;
        powerExtra = 300f;
        convertList.add(new Item[]{Items.sand, Items.silicon}, new Item[]{Items.coal, Items.graphite}, new Item[]{Items.beryllium, Items.oxide});
    }

    public class RefineModuleBuild extends DrillModuleBuild{
        public Rand rand = new Rand();
        @Override
        public void draw() {
            super.draw();
            Lines.stroke(circleStroke * smoothWarmup);

            float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
            float a = alpha * smoothWarmup;
            Draw.blend(Blending.additive);

            Draw.color(midColor, a);
            Fill.circle(x, y, flameRad + si);

            Draw.color(flameColor, a);
            Lines.circle(x, y, (flameRad + circleSpace + si) * smoothWarmup);

            rand.setSeed(id);
            float base = (Time.time / particleLife);
            for(int i = 0; i < particles; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
                float len = particleRad * particleInterp.apply(fout);
                Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                Fill.circle(
                    x + Angles.trnsx(angle, len),
                    y + Angles.trnsy(angle, len),
                    particleSize * fin * smoothWarmup
                );
            }

            Draw.blend();
            Draw.reset();
        }
    }
}
