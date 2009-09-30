# Script for church priest Manard on Tutorial Island.

from Atrinik import *
import string, os
from inspect import currentframe

# The activator
activator = WhoIsActivator()
me = WhoAmI()

execfile(os.path.dirname(currentframe().f_code.co_filename) + "/quests.py")

quest_arch_name = quest_items["priest_manard"]["arch_name"]
quest_item_name = quest_items["priest_manard"]["item_name"]

# Get the message
msg = WhatIsMessage().strip().lower()

# Split the message to words
text = string.split(msg)

qitem = activator.CheckQuestObject(quest_arch_name, quest_item_name)
item = activator.CheckInventory(1, quest_arch_name, quest_item_name)

if text[0] == "healing":
	if activator.GetGod() == "Tabernacle":
		spell = GetSpellNr("minor healing")

		if spell == -1:
			me.SayTo(activator, "\nUnknown spell.")
		else:
			if activator.DoKnowSpell(spell) == 1:
				me.SayTo(activator, "\nYou already know this prayer...")
			else:	
				activator.AcquireSpell(spell, LEARN)
	else:
		me.SayTo(activator, "\nI will listen to you once you join the deity of Tabernacle.")

elif text[0] == "cause":
	if activator.GetGod() == "Tabernacle":
		if qitem != None or item == None:
			me.SayTo(activator, "\nI can't teach you this now.")
		else:
			spell = GetSpellNr("cause light wounds")

			if spell == -1:
				me.SayTo(activator, "\nUnknown spell.")
			else:
				activator.AddQuestObject(quest_arch_name, quest_item_name)
				item.Remove()
				me.SayTo(activator, "\nHere we go!")

				if activator.DoKnowSpell(spell) == 1:
					me.SayTo(activator, "\nYou already know this prayer...")
				else:	
					activator.AcquireSpell(spell, LEARN)
	else:
		me.SayTo(activator, "\nI will listen to you once you join the deity of Tabernacle.")

elif text[0] == "quest":
	if activator.GetGod() == "Tabernacle":
		if qitem != None:
			me.SayTo(activator, "\nI have no more quests for you.")
		else:
			if item == None:
				me.SayTo(activator, "\nMy apprentice went with some guards to explore the hole you might have noticed in the southwest area.\nHe was the only one who returned, and as Captain Regulus probably told you, all the guards that he sent were slain.\nHowever, the apprentice lost a prayerbook, containing the wisdom of the cause light wounds prayer.\nIf you can find the book and return it to me, I will teach you the prayer of cause light wounds.\nI suggest you first do quest Captain Regulus has first, if you have not already.")
			else:
				me.SayTo(activator, "\nYou found the book! Very good. Say ^cause^ now to learn the prayer of cause light wounds, as your reward.")
	else:
		me.SayTo(activator, "\nI will listen to you once you join the deity of Tabernacle.")
	
elif msg == "cast":
	if activator.GetGod() == "Tabernacle":
		me.SayTo(activator, "\nTo cast a prayer you need a deity.\nYou should be a follower of the Tabernacle by now.\n That should be written under your character name.\nYou can cast a spell or prayer in two ways:\nYou can type /cast <spellname> in the console.\nIn our case /cast minor healing.\nOr you can select the spell menu with F9.\nGo to the entry minor healing and press return over it.\nThen you can use it in the range menu like throwing.")
	else:
		me.SayTo(activator, "\nI will listen to you once you join the deity of Tabernacle.")

elif msg == "hello" or msg == "hi" or msg == "hey":
	if activator.GetGod() == "Tabernacle":
		me.SayTo(activator, "\nVery good. Now listen:\nI will teach you the prayer ~minor healing~ if you say ^healing^ to me.\nBut first you should ask me how to ^cast^ spells and prayers.\nI will tell you the ways you can cast a prayer or spell. When you have learned that, say ^quest^ to me.")
	else:
		me.SayTo(activator, "\nWelcome to the church of the Tabernacle.\nTo access the powers of the Tabernacle, you have to apply altar of Tabernacle. One is over there, west of me.\nStep over it and apply it. Then come back to me.")