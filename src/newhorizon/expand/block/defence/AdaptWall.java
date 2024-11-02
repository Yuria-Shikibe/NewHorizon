package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.NHFx;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;
import static mindustry.Vars.net;
import static newhorizon.util.graphic.SpriteUtil.*;

public class AdaptWall extends Wall {
	public TextureRegion[] atlasRegion;
	public TextureRegion[] topLargeRegion, topSmallRegion, topHorizontalRegion, topVerticalRegion;

	public float maxShareStep = 3;

	private final Seq<Building> toDamage = new Seq<>();
	private final Queue<Building> queue = new Queue<>();

	public AdaptWall(String name){
		super(name);
		size = 1;
		insulated = true;
		absorbLasers = true;
		placeableLiquid = true;
		crushDamageMultiplier = 1f;
	}
	
	@Override
	public void load(){
		super.load();
		atlasRegion = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32, 0, ATLAS_INDEX_4_12);
		topLargeRegion = new TextureRegion[2];
		topSmallRegion = new TextureRegion[2];
		topHorizontalRegion = new TextureRegion[2];
		topVerticalRegion = new TextureRegion[2];
		for (int i = 0; i < 2; i++){
			topLargeRegion[i] = Core.atlas.find(name + "-top-large-" + i);
			topSmallRegion[i] = Core.atlas.find(name + "-top-small-" + i);
			topHorizontalRegion[i] = Core.atlas.find(name + "-top-horizontal-" + i);
			topVerticalRegion[i] = Core.atlas.find(name + "-top-vertical-" + i);
		}
	}
	
	public class AdaptWallBuild extends Building{
		public Seq<AdaptWallBuild> connectedWalls = new Seq<>();
		public int drawIndex = 0;
		public int topIdx = 0;

		public void updateDrawRegion(){
			drawIndex = 0;

			for(int i = 0; i < orthogonalPos.length; i++){
				Point2 pos = orthogonalPos[i];
				Building build = Vars.world.build(tileX() + pos.x, tileY() + pos.y);
				if (checkWall(build)){
					drawIndex += 1 << i;
				}
			}
			for(int i = 0; i < diagonalPos.length; i++){
				Point2[] posArray = diagonalPos[i];
				boolean out = true;
				for (Point2 pos : posArray) {
                    Building build = Vars.world.build(tileX() + pos.x, tileY() + pos.y);
                    if (!(checkWall(build))) {
						out = false;
                        break;
                    }
                }
				if (out){
					drawIndex += 1 << i + 4;

				}
			}

			drawIndex = ATLAS_INDEX_4_12_MAP.get(drawIndex);
			updateTopIndex();
		}

		public void updateTopIndex(){
			topIdx = 0;
			if (tileX() % 4 < 2){
				if (tileX() % 4 == 0 && tileY() % 2 == 0 && validTile(1, 0) && validTile(1, 1) && validTile(0, 1)){topIdx = 1; return;}
				if (tileX() % 4 == 1 && tileY() % 2 == 0 && validTile(-1, 0) && validTile(0, 1) && validTile(-1, 1)){topIdx = 0; return;}
				if (tileX() % 4 == 1 && tileY() % 2 == 1 && validTile(-1, 0) && validTile(-1, -1) && validTile(0, -1)){topIdx = 0; return;}
				if (tileX() % 4 == 0 && tileY() % 2 == 1 && validTile(1, 0) && validTile(1, -1) && validTile(0, -1)){topIdx = 0; return;}
			}else{
				if (tileX() % 4 == 2 && tileY() % 2 == 1 && validTile(1, 0) && validTile(1, 1) && validTile(0, 1)){topIdx = 2; return;}
				if (tileX() % 4 == 3 && tileY() % 2 == 1 && validTile(-1, 0) && validTile(0, 1) && validTile(-1, 1)){topIdx = 0; return;}
				if (tileX() % 4 == 3 && tileY() % 2 == 0 && validTile(-1, 0) && validTile(-1, -1) && validTile(0, -1)){topIdx = 0; return;}
				if (tileX() % 4 == 2 && tileY() % 2 == 0 && validTile(1, 0) && validTile(1, -1) && validTile(0, -1)){topIdx = 0; return;}

				if (tileX() % 4 == 2 && tileY() % 2 == 0 && validTile(1, 0) && validTile(1, 1) && validTile(0, 1)){topIdx = 1; return;}
				if (tileX() % 4 == 3 && tileY() % 2 == 0 && validTile(-1, 0) && validTile(0, 1) && validTile(-1, 1)){topIdx = 0; return;}
				if (tileX() % 4 == 3 && tileY() % 2 == 1 && validTile(-1, 0) && validTile(-1, -1) && validTile(0, -1)){topIdx = 0; return;}
				if (tileX() % 4 == 2 && tileY() % 2 == 1 && validTile(1, 0) && validTile(1, -1) && validTile(0, -1)){topIdx = 0; return;}

			}


			if(tileX() % 2 == 0 && tileY() % 2 == 1 && validTile(1, 0)){topIdx = 5; return;}
			if(tileX() % 2 == 1 && tileY() % 2 == 1 && validTile(-1, 0)){topIdx = 0; return;}

			if(tileX() % 2 == 0 && tileY() % 2 == 0 && validTile(1, 0)){topIdx = 6; return;}
			if(tileX() % 2 == 1 && tileY() % 2 == 0 && validTile(-1, 0)){topIdx = 0; return;}

			//if(tileY() % 2 == 1 && tileX() % 2 == 1 && validTile(0, 1)){topIdx = 7; return;}
			//if(tileY() % 2 == 0 && tileX() % 2 == 1 && validTile(0, -1)){topIdx = 0; return;}
			//if(tileY() % 2 == 1 && tileX() % 2 == 0 && validTile(0, 1)){topIdx = 8; return;}
			//if(tileY() % 2 == 0 && tileX() % 2 == 0 && validTile(0, -1)){topIdx = 0; return;}


			topIdx = (tileX() + tileY()) % 2 == 0? 3: 4;
		}

		public void drawTop(){
			if (topIdx == 0) return;
			if (topIdx == 1) {Draw.rect(topLargeRegion[0], x + tilesize/2f, y + tilesize/2f);}
			if (topIdx == 2) {Draw.rect(topLargeRegion[1], x + tilesize/2f, y + tilesize/2f);}
			if (topIdx == 3) {Draw.rect(topSmallRegion[0], x, y);}
			if (topIdx == 4) {Draw.rect(topSmallRegion[1], x, y);}
			if (topIdx == 5) {Draw.rect(topHorizontalRegion[0], x + tilesize/2f, y);}
			if (topIdx == 6) {Draw.rect(topHorizontalRegion[1], x + tilesize/2f, y);}
			if (topIdx == 7) {Draw.rect(topVerticalRegion[0], x, y + tilesize/2f);}
			if (topIdx == 8) {Draw.rect(topVerticalRegion[1], x, y + tilesize/2f);}
		}

		public boolean validTile(int x, int y){
			return world.build(tileX() + x, tileY() + y) != null && world.build(tileX() + x, tileY() + y).block == block();
		}

		public void findLinkWalls(){
			toDamage.clear();
			queue.clear();

			queue.addLast(this);
			while (queue.size > 0) {
				Building wall = queue.removeFirst();
				toDamage.add(wall);
				for (Building next : wall.proximity) {
					if (linkValid(next) && !toDamage.contains(next)) {
						toDamage.add(next);
						queue.addLast(next);
					}
				}
			}
		}

		public boolean linkValid(Building build){
			return checkWall(build) && Mathf.dstm(tileX(), tileY(), build.tileX(), build.tileY()) <= maxShareStep;
		}

		public boolean checkWall(Building build){
			return build != null && build.block == this.block;
		}

		@Override
		public void drawSelect() {
			super.drawSelect();
			findLinkWalls();
			for (Building wall: toDamage){
				Fill.square(wall.x, wall.y, 2);
			}
		}

		public void updateProximityWall(){
			connectedWalls.clear();

			for (Point2 point : proximityPos) {
				Building other = world.build(tile.x + point.x, tile.y + point.y);
				if (other == null || other.team != team) continue;
				if (checkWall(other)) {
					connectedWalls.add((AdaptWallBuild) other);
				}
			}

			updateDrawRegion();
		}
		
		public void drawTeam() {
			Draw.color(this.team.color);
			Draw.z(Layer.blockUnder);
			Fill.square(x, y, 5f);
			Draw.color();
		}

		@Override
		public boolean collision(Bullet other){
			if(other.type.absorbable)other.absorb();
			return super.collision(other);
		}
		
		@Override
		public float handleDamage(float amount){
			findLinkWalls();
			float shareDamage = amount / toDamage.size;
			for (Building b: toDamage){
				damageShared(b, shareDamage);
			}
			NHFx.shareDamage.at(x, y, block.size * tilesize / 2f, team.color, Mathf.clamp(shareDamage/(block.health * 0.1f)));
			return shareDamage;
		}

		public void damageShared(Building building, float damage) {
			if (building.dead()) return;
			float dm = state.rules.blockHealth(team);
			if (Mathf.zero(dm)) {
				damage = building.health + 1;
			} else {
				damage /= dm;
			}
			if (!net.client()) {
				building.health -= damage;
			}
			healthChanged();
			if (building.health <= 0) {
				Call.buildDestroyed(building);
			}
			NHFx.shareDamage.at(building.x, building.y, building.block.size * tilesize / 2f, team.color, Mathf.clamp(damage/(block.health * 0.1f)));
		}
		
		@Override
		public void draw(){
			drawTop();
			Draw.z(Layer.block + 1f);
			Draw.rect(atlasRegion[drawIndex], x, y);
		}

		public void updateProximity() {
			super.updateProximity();

			updateProximityWall();
			for (AdaptWallBuild other : connectedWalls) {
				other.updateProximityWall();
			}
		}
		
		@Override
		public void onRemoved(){
			for (AdaptWallBuild other : connectedWalls) {
				other.updateProximityWall();
			}
			super.onRemoved();
		}
	}
}