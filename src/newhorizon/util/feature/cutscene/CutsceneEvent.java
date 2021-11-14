package newhorizon.util.feature.cutscene;

import arc.func.Boolp;
import arc.func.Cons;
import arc.math.geom.Position;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.ArcRuntimeException;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import newhorizon.util.feature.cutscene.annotation.HeadlessDisabled;

/**
 *
 *
 *
 * */
public class CutsceneEvent implements Cloneable{
	public static final ObjectMap<String, CutsceneEvent> cutsceneEvents = new ObjectMap<>();
	
	public static CutsceneEvent get(String name){
		return cutsceneEvents.get(name);
	}
	
	public static final CutsceneEvent NULL_EVENT = new CutsceneEvent("NULL_EVENT"){{
		removeAfterTriggered = true;
		isHidden = true;
		cannotBeRemove = true;
		updatable = false;
		drawable = false;
		exist = () -> true;
	}};
	
	public boolean cannotBeRemove = false;
	public String name;
	
	public Position position;
	public float reloadTime;
	
	public boolean initOnce = true;
	public boolean removeAfterTriggered = false;
	public boolean removeAfterVictory = true;
	public boolean isHidden = false;
	public boolean updatable = true, drawable = false;
	public Boolp exist = () -> true;
	
	protected CutsceneEvent(String name, boolean register){
		this.name = name;
		if(register){
			cutsceneEvents.put(name, this);
		}
	}
	
	public CutsceneEvent(String name){
		this(name, true);
	}
	
	public CutsceneEvent(){this("null", false);}
	
	@SuppressWarnings("UnusedReturnValue")
	public CutsceneEventEntity setup(){
		if(name.equals("null"))throw new ArcRuntimeException("Illegal Event #" + System.identityHashCode(this) + "[!]RENAME IT!");
		CutsceneEventEntity entity = Pools.obtain(CutsceneEventEntity.class, CutsceneEventEntity::new);
		entity.setType(this);
		
		if(!Vars.net.client())entity.add();
		if(!UIActions.disabled())onCallUI(entity);
		
		return entity;
	}
	
	public void draw(CutsceneEventEntity e){
	
	}
	
	public void updateEvent(CutsceneEventEntity e){
	
	}
	
	public void onRemove(CutsceneEventEntity e){
	
	}
	
	public void onCall(CutsceneEventEntity e){
	
	}
	
	public void onCallUI(CutsceneEventEntity e){
	
	}
	
	@HeadlessDisabled
	public void setupTable(CutsceneEventEntity e, Table table){
	
	}
	
	@HeadlessDisabled
	public void removeTable(CutsceneEventEntity e, Table table){
		e.infoT.remove();
	}
	
	public void triggered(CutsceneEventEntity e){
	
	}
	
	public void afterRead(CutsceneEventEntity e){
	
	}
	
	public void afterSync(CutsceneEventEntity e){
	
	}
	
	public void write(CutsceneEventEntity e, Writes writes){
	
	}
	
	public void read(CutsceneEventEntity e, Reads reads){
	
	}
	
	public void setType(CutsceneEventEntity e){
	
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	public <T extends CutsceneEvent> T copyAnd(Class<T> c, String name, Cons<T> modifier){
		try{
			T clone = (T)super.clone();
			clone.name = name;
			cutsceneEvents.put(name, clone);
			modifier.get(clone);
			
			return clone;
		}catch(CloneNotSupportedException e){
			throw new AssertionError();
		}
	}
}
