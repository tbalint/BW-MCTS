package bwmcts.sparcraft;

import javabot.types.UnitSizeType;
import javabot.types.WeaponType;

public class PlayerWeapon {
	PlayerProperties player;
	WeaponType	type;

	public PlayerWeapon(PlayerProperties player, WeaponType type) {
	    this.player=player;
	    this.type=type; 
	}

	public int	GetDamageBase()										
	{ 
	    return WeaponProperties.Get(type).GetDamageBase(player); 
	}

	public float GetDamageMultiplier(UnitSizeType targetSize)	
	{ 
	    return WeaponProperties.Get(type).GetDamageMultiplier(targetSize); 
	}
	public float GetDamageMultiplier(int targetSize)	
	{ 
	    return WeaponProperties.Get(type).GetDamageMultiplier(targetSize); 
	}

	public int	GetCooldown(){ 
	    return WeaponProperties.Get(type).GetCooldown(player); 
	}

	public int	GetMaxRange(){ 
	    return WeaponProperties.Get(type).GetMaxRange(player); 
	}
}
