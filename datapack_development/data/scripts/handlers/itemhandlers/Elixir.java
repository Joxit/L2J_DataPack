/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TimeStamp;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Elixir implements IItemHandler
{
	protected static final Logger _log = Logger.getLogger(Elixir.class.getName());
	
	private static final int[] ITEM_IDS =
	{
		//elixir of life
		8622, 8623, 8624, 8625, 8626, 8627,
		//elixir of Strength
		8628, 8629, 8630, 8631, 8632, 8633,
		//elixir of cp
		8634, 8635, 8636, 8637, 8638, 8639,
		//Secret medicine of Will
		20354, 20355, 20356, 20357, 20358,
		//Secret medicine of Life
		20359, 20360, 20361, 20362, 20363,
		//Secret medicine of Will
		22027, 22028, 22029, 22030, 22031,
		//Secret medicine of Life
		22032, 22033, 22034, 22035, 22036
	};
	
	/**
	 * 
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.actor.L2Playable, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar; // use activeChar only for L2PcInstance checks where cannot be used PetInstance
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else
		{
			playable.sendPacket(new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}
		
		if (!TvTEvent.onPotionUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (playable.isAllSkillsDisabled())
		{
			ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case 8622:
			case 8623:
			case 8624:
			case 8625:
			case 8626:
			case 8627:
				if (usePotion(activeChar, 2287, 2288, itemId-8621))
					activeChar.sendPacket(new ExUseSharedGroupItem(itemId, 1, 300000, 300000));
				break;
			case 8628:
			case 8629:
			case 8630:
			case 8631:
			case 8632:
			case 8633:
				if (usePotion(activeChar, 2288, 2287, itemId-8627))
					activeChar.sendPacket(new ExUseSharedGroupItem(itemId, 1, 300000, 300000));
				break;
			case 8634:
			case 8635:
			case 8636:
			case 8637:
			case 8638:
			case 8639:
				if (usePotion(activeChar, 2289, 0, itemId-8633))
					activeChar.sendPacket(new ExUseSharedGroupItem(itemId, 2, 300000, 300000));
				break;
			case 22027:
			case 22028:
			case 22029:
			case 22030:
			case 22031:
				if (usePotion(activeChar, 26027, 0, itemId-22025))
					activeChar.sendPacket(new ExUseSharedGroupItem(itemId, 2, 300000, 300000));
				break;
			case 22032:
			case 22033:
			case 22034:
			case 22035:
			case 22036:
				if (usePotion(activeChar, 26028, 0, itemId-22030))
					activeChar.sendPacket(new ExUseSharedGroupItem(itemId, 1, 300000, 300000));
				break;
			case 20354:
			case 20355:
			case 20356:
			case 20357:
			case 20358:
				if (usePotion(activeChar, 22043, 0, itemId-20352))
					activeChar.sendPacket(new ExUseSharedGroupItem(itemId, 2, 300000, 300000));
				break;
			case 20359:
			case 20360:
			case 20361:
			case 20362:
			case 20363:
				if (usePotion(activeChar, 22044, 0, itemId-20357))
					activeChar.sendPacket(new ExUseSharedGroupItem(itemId, 1, 300000, 300000));
				break;
				
		}
	}
	
	/**
	 * 
	 * @param activeChar
	 * @param magicId
	 * @param level
	 * @return
	 */
	public boolean usePotion(L2PcInstance activeChar, int magicId, int skillToCheck, int level)
	{
		boolean check = skillToCheck != 0 ? true : false;
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
			if (!skill.checkCondition(activeChar, activeChar, false))
	        	return false;
			// Return false if potion is in reuse
			// so is not destroyed from inventory
			if (activeChar.isSkillDisabled(skill.getId()))
			{
				reuse(activeChar,skill);
				return false;
			}
			if (check)
			{
				if ( activeChar.isSkillDisabled(skillToCheck))
				{
					reuse(activeChar,skill);
					return false;
				}
				int reuseDelay = skill.getReuseDelay();
				activeChar.disableSkill(skillToCheck, reuseDelay);
				activeChar.addTimeStamp(skillToCheck, reuseDelay);
			}
			activeChar.doSimultaneousCast(skill);
			return true;
		}
		return false;
	}
	
	private void reuse(L2PcInstance player,L2Skill skill)
	{
		SystemMessage sm = null;
    	FastMap<Integer, TimeStamp> timeStamp = player.getReuseTimeStamp();
			
    	if (timeStamp != null && timeStamp.containsKey(skill.getId()))
    	{
    		int remainingTime = (int)(player.getReuseTimeStamp().get(skill.getId()).getRemaining()/1000);
    		int minutes = (remainingTime%3600)/60;
    		int seconds = (remainingTime%60);
    		if (minutes > 0)
    		{
    			sm = new SystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
    			sm.addSkillName(skill);
    			sm.addNumber(minutes);
    		}
    		else
    		{
    			sm = new SystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
    			sm.addSkillName(skill);
    		}
    		sm.addNumber(seconds);
    	}
    	else
    	{
    		sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
    		sm.addSkillName(skill);
    	}
    	player.sendPacket(sm);
	}
	/**
	 * 
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#getItemIds()
	 */
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
