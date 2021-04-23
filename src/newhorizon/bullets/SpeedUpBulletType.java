package newhorizon.bullets;

import arc.math.Mathf;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import newhorizon.interfaces.Curve;

public class SpeedUpBulletType extends BasicBulletType{
	public float velocityBegin = -1;
	public float velocityEnd = -1;
	public float accelerateBegin = 0.1f;
	public float accelerateEnd = 0.6f;
	
	public Curve<Bullet> accelCurve;
	
	public SpeedUpBulletType(){
		super();
	}
	
	public SpeedUpBulletType(float speed, float damage, String bulletSprite) {
		super(speed, damage, bulletSprite);
	}
	
	public SpeedUpBulletType(float speed, float damage) {
		this(speed, damage, "bullet");
	}
	
	@Override
	public void init(){
		super.init();
		if(velocityBegin < 0)velocityBegin = speed;
		if(velocityEnd < 0)velocityEnd = speed;
		if(accelCurve == null)accelCurve = b -> Mathf.curve(b.fin(), accelerateBegin, accelerateEnd);
		speed = (velocityBegin * lifetime * accelerateBegin + (velocityBegin + velocityEnd) / 2f * lifetime * Mathf.clamp(accelerateEnd - accelerateBegin) + velocityEnd * lifetime * (1 - accelerateEnd)) / lifetime;
	}
	
	@Override
	public void update(Bullet b){
		b.vel.setLength(velocityBegin + accelCurve.get(b) * velocityEnd);
		super.update(b);
	}
}
