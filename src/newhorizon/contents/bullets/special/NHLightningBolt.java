package newhorizon.contents.bullets.special;

import arc.struct.Seq;
import arc.func.Cons;
import arc.util.Tmp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Rect;
import arc.graphics.Color;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.gen.Unitc;
import mindustry.gen.Bullet;
import mindustry.entities.Units;
import mindustry.entities.Effect;
import mindustry.entities.Damage;
import mindustry.entities.Lightning;

public class NHLightningBolt { //Provide some workable methods to genetic position to position lightning bolt. Powered by Yuria.

	/**
	* Main methods:
	*
	*	 Radius:
	* 		generateRange(Position owner, Team team, float range, int hits, int boltNum, float damage, Color color, boolean chance, float width);
	* 		generateRange( Bullet  owner,   [N/A]	float range, int hits, int boltNum, float damage, Color color, boolean chance, float width);
	*
	* 	Single:
	* 		generate(Position owner, Position target, Team team, float damage, Color color, boolean createLightning, float width	 [N/A]		   	[N/A]    	);
	* 		generate(Position owner, Position target, Team Team, 	[N/A] 	Color color,		  [N/A]	   	float width, int boltNum, Cons<Position> movement);
	* 		generate( Bullet  owner, Position target,   [N/A]	float damage, Color color, boolean createLightning, float width	 [N/A]		   	[N/A]    	);
	*
	*/


	//ELEMENTS
	
	//Default effect lifetime.
	public static final float BOLTLIFE = 20f;
	//Default lightning width.
	public static final float WIDTH = 2.2f;
	//Default min lightning generate distance from targetA to B.
	public static final float GENERATE_DST = 19f;
	//Default randX mult coefficient.
	public static final float RANDOM_RANGE_MULT_COEFFCIENT = 4f;

	//Used in find target method.
	private static Tile furthest;


	//METHODS
	
	//Compute the proper homologous Tile.position's x and y.
	public static int toIntTile(float pos) {
		return Math.round(pos / Vars.tilesize);
	}

	//Compute the proper hit position.
	public static Position findInterceptedPoint(Position from, Position target, Team fromTeam) {
		Tmp.v1.trns(from.angleTo(target), from.dst(target));

		furthest = null;

		return 
		Vars.world.raycast(
			toIntTile(from.getX()),
			toIntTile(from.getY()),
			toIntTile(from.getX() + Tmp.v1.x),
			toIntTile(from.getY() + Tmp.v1.y),
			(x, y) -> (furthest = Vars.world.tile(x, y)) != null && furthest.block().absorbLasers && furthest.team() != fromTeam
		) && furthest != null ? furthest : target;
	}

	//Set the range of lightning's color's lerp, randX.
	public static float getBoltRandomRange() {
		return Mathf.random(2f, 5f);
	}

	//generate lightning to the enemies in range.
	public static void generateRange(Position owner, Team team, float range, int hits, int boltNum, float damage, Color color, boolean chance, float width) {
		Rect rect = new Rect();
		Seq<Unitc> entities = new Seq<>();
		Units.nearbyEnemies(team, rect.setSize(range * 2).setCenter(owner.getX(), owner.getY()), unit -> {
			whetherAdd(entities, unit, hits);
		});
		for (Unitc unit : entities) {
			for (int i = 0; i < boltNum; i ++) {
				generate(owner, unit, team, damage, color, chance, width);
			}
		}
	}

	//A radius generate method that with a Bullet owner.
	public static void generateRange(Bullet owner, float range, int hits, int boltNum, float damage, Color color, boolean chance, float width) {
		Rect rect = new Rect();
		Seq<Unitc> entities = new Seq<>();
		Units.nearbyEnemies(owner.team(), rect.setSize(range * 2).setCenter(owner.getX(), owner.getY()), unit -> {
			whetherAdd(entities, unit, hits);
		});
		for (Unitc unit : entities) {
			for (int i = 0; i < boltNum; i ++) {
				generate(owner, unit, damage, color, chance, width);
			}
		}
	}

	//generate position to position lightning and deals splash damage, create none target lightning.
	public static void generate(Position owner, Position target, Team team, float damage, Color color, boolean createLightning, float width) {
		Position sureTarget = findInterceptedPoint(owner, target, team);
		float dst = owner.dst(sureTarget);
		float multBolt = getBoltRandomRange();
		float randRange = multBolt * RANDOM_RANGE_MULT_COEFFCIENT;

		Seq<Float> randomArray = new Seq<>();
		for (int num = 0; num < dst / (Vars.tilesize * multBolt) + 1; num ++) {
			randomArray.add(Mathf.range(randRange));
		}

		if (createLightning)Lightning.create(team, color, damage, sureTarget.getX(), sureTarget.getY(), Mathf.random(360), Mathf.random(8, 12));
		Damage.damage(team, sureTarget.getX(), sureTarget.getY(), 20f, damage * 1.5f);
		createBoltEffect(color, width,
			new NHLightningBoltEffectData(randomArray, sureTarget, owner)
		);
	}

	//A generate method that could set lightning number and extra movements to the final target.
	public static void generate(Position owner, Position target, Team team, Color color, float width, int boltNum, Cons<Position> movement) {
		Position sureTarget = findInterceptedPoint(owner, target, team);
		movement.get(sureTarget);

		float dst = owner.dst(sureTarget);
		for (int i = 0; i < boltNum; i ++) {
			float multBolt = getBoltRandomRange();
			float randRange = multBolt * RANDOM_RANGE_MULT_COEFFCIENT;

			Seq<Float> randomArray = new Seq<>();
			for (int num = 0; num < dst / (Vars.tilesize * multBolt) + 1; num ++) {
				randomArray.add(Mathf.range(randRange));
			}
			createBoltEffect(color, width, new NHLightningBoltEffectData(randomArray, sureTarget, owner));
		}
	}

	//A generate method that with a Bullet owner.
	public static void generate(Bullet owner, Position target, float damage, Color color, boolean createLightning, float width) {
		generate(owner, target, owner.team(), damage, color, createLightning, width);
	}

	//Protected methods and classes.

	//Add proper unit into the to hit Seq.
	protected static void whetherAdd(Seq<Unitc> entities, Unit unit, int hits) {
		if (
			entities.size <= hits &&
			(
				entities.isEmpty() || //Make sure add the started one.
				unit.dst(Geometry.findClosest(unit.x, unit.y, entities)) > GENERATE_DST
			)
		)entities.add(unit);
	}

	//generate lightning effect.
	protected static void createBoltEffect(Color color, float width, NHLightningBoltEffectData createData) {
		new Effect(BOLTLIFE, createData.owner.dst(createData.target) * 2, e -> {
			NHLightningBoltEffectData data = (NHLightningBoltEffectData)e.data;
			Position target = data.target, from = data.owner;
			Seq<Float> randomVec = data.randomVec;

			float dstFr = target.dst(e.x, e.y);
			int sigs = randomVec.size;

			Draw.color(e.color);
			Lines.stroke(width * e.fout());
			Vec2 last = new Vec2();
			Fill.circle(from.getX() + last.x, from.getY() + last.y, Lines.getStroke() * 1.12f);
			for (int i = 1; i < sigs - 1; i ++) {
				Tmp.v2.trns(from.angleTo(target), (dstFr / sigs) * (i), randomVec.get(i));
				Lines.line(from.getX() + last.x, from.getY() + last.y, from.getX() + Tmp.v2.x, from.getY() + Tmp.v2.y, false);
				Fill.circle(from.getX() + Tmp.v2.x, from.getY() + Tmp.v2.y, Lines.getStroke() / 2f);
				last.set(Tmp.v2);
			}
			Lines.line(from.getX() + last.x, from.getY() + last.y, target.getX(), target.getY(), false);
			Fill.circle(target.getX(), target.getY(), Lines.getStroke() / 2f);
		}).at(createData.owner.getX(), createData.owner.getY(), 0, color, createData);

	}

	//Effect data
	protected static class NHLightningBoltEffectData {
		public Seq<Float> randomVec;
		public Position owner, target;

		public NHLightningBoltEffectData(Seq<Float> randomVec, Position target, Position owner) {
			this.randomVec = randomVec;
			this.target = target;
			this.owner = owner;
		}
	}
}
