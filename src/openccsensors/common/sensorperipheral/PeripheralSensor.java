package openccsensors.common.sensorperipheral;

import java.util.Arrays;

import cpw.mods.fml.common.FMLCommonHandler;

import openccsensors.common.api.ISensorAccess;
import openccsensors.common.api.ISensorCard;
import openccsensors.common.api.ISensorInterface;
import openccsensors.common.core.ISensorEnvironment;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;
import dan200.turtle.api.ITurtlePeripheral;

public class PeripheralSensor
implements ITurtlePeripheral, ISensorAccess
{
	
	private ISensorEnvironment env;
	
	private ISensorInterface sensorCard;
	
	private ItemStack sensorItemStack;
	
	private boolean turtle;
	
	private boolean directional;

	public PeripheralSensor(ISensorEnvironment _env, boolean _turtle) 
	{
		env = _env;
		turtle = _turtle;
		directional = false;
	}
	
	public boolean isDirectional()
	{
		return directional;
	}
	
	public void setDirectional(boolean isDirectional)
	{
		directional = isDirectional;
		Vec3 loc = env.getLocation();
		World world = env.getWorld();
		if (world != null) // can happen during loading
			world.markBlockForUpdate((int)loc.xCoord, (int)loc.yCoord, (int)loc.zCoord);
	}
	
	public int getDirection()
	{
		int direction = 0;
		if (turtle) {
			// think of something for turtles...
		} else {
			Vec3 loc = env.getLocation();
			World world = env.getWorld();
				if (world != null) // can happen during loading
				{
					TileEntity tile = world.getBlockTileEntity((int)loc.xCoord, (int)loc.yCoord, (int)loc.zCoord);
					if (tile != null && tile instanceof TileEntitySensor) {
						direction = ((TileEntitySensor)tile).getOrientation();
					}
				}
		}
		return direction;
	}	

	@Override
	public String getType() 
	{
		return "sensor";
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] { "getTargets", "getTargetDetails", "getSensorName", "getSensorMethods", "sensorCardCall" };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		if (sensorItemStack != env.getSensorCard())
		{
			sensorItemStack = env.getSensorCard();
			sensorCard = null;
		}
		
		if (sensorCard == null)
		{
			sensorCard = getSensorCard();
		}
		
		if (sensorCard == null)
			throw new Exception ("No sensor card inserted.");
		
		Vec3 vec = env.getLocation();
		
		switch (method)
		{
			case 0:
				return new Object[] { sensorCard.getBasicTarget(env.getWorld(), (int) vec.xCoord, (int) vec.yCoord, (int) vec.zCoord) };
			case 1:
				if (arguments.length > 0)
				{
					if (arguments[0] instanceof String)
					{
						return new Object[]{ sensorCard.getTargetDetails(env.getWorld(), (int) vec.xCoord, (int) vec.yCoord, (int) vec.zCoord, arguments[0].toString()) };
					}
				}
				throw new Exception("Invalid arguments. Expected String.");
			case 2:
				return new Object[]{ sensorCard.getName() };
			case 3:
				return sensorCard.getMethods();
			case 4:
				if (arguments.length > 0)
				{
					if (arguments[0] instanceof String)
					{
						String[] sensorMethods = sensorCard.getMethods();
						int newMethod = Arrays.asList(sensorMethods).indexOf(arguments[0].toString());
						
						if (method < 0)
							throw new Exception("Invalid sensor command");
						
						
						
						Object[] newArray = new Object[arguments.length - 1];
						System.arraycopy(arguments, 1, newArray, 0, arguments.length - 1);
						
						return sensorCard.callMethod(this, newMethod, newArray);
					}
				}
				throw new Exception("Invalid arguments. Expected String.");
		}
		
		return null;
	}

	@Override
	public boolean canAttachToSide(int side) 
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer, String computerSide) 
	{
			//computer.mountFixedDir("ocs", "openccsensors/resources/lua", true);
	}

	@Override
	public void detach(IComputerAccess computer) 
	{
		
	}

	@Override
	public void update() 
	{
	}
	
	private ISensorInterface getSensorCard() throws Exception
	{
		ItemStack itemstack = env.getSensorCard();
		
		if (itemstack == null)
		{
			throw new Exception("No item in sensor reader!");
		}
		
		Item item = itemstack.getItem(); 
		if (item instanceof ISensorCard)
		{
			ISensorInterface sensor = ((ISensorCard)item).getSensorInterface(itemstack, turtle);
			
			if (sensor != null)
			{
				return sensor;
			}
			
			throw new Exception("Error reading sensor - please bug the author to add an ISensorCard!");
		}
		
		throw new Exception("Item is not sensor card!");
	}

	@Override
	public boolean isTurtle()
	{
		return turtle;
	}

	@Override
	public ISensorEnvironment getSensorEnvironment()
	{
		return env;
	}

}
