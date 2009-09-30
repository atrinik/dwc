from Atrinik import *
import string, os
from inspect import currentframe

activator = WhoIsActivator()
me = WhoAmI()

execfile(os.path.dirname(currentframe().f_code.co_filename) + "/quests.py")

quest_arch_name = quest_items["mercenary_chereth"]["arch_name"]
quest_item_name = quest_items["mercenary_chereth"]["item_name"]

msg = WhatIsMessage().strip().lower()
text = string.split(msg)

qitem = activator.CheckQuestObject(quest_arch_name, quest_item_name)
item = activator.CheckInventory(1, quest_arch_name, quest_item_name)

# Common function to finish the quest.
def finish_quest():
	activator.AddQuestObject(quest_arch_name, quest_item_name)
	item.Remove()
	me.SayTo(activator, "Here we go!")
	me.map.Message(me.x, me.y, MAP_INFO_NORMAL, "Chereth teaches some ancient skill.", COLOR_YELLOW)

# Explain archery skills
if text[0] == "archery":
	me.SayTo(activator, "\nYes, there are three archery skills:\nBow archery is the most common firing arrows.\nSling archery allows fast firing stones with less damage.\nCrossbow archery uses crossbows and bolts. Slow but powerful.")

# Give out a link to the quest
elif text[0] == "learn":
	if qitem != None:
		me.SayTo(activator, "\nSorry, I can only teach you |one| archery skill.")
	else:
		me.SayTo(activator, "\nWell, there are three different ^archery^ skills.\nI can teach you only |one| of them.\nYou have to stay with it then. So choose wisely.\nI can tell you more about ^archery^. But before I teach you I have a little ^quest^ for you.");

# Teach bow archery
elif msg == "teach me bow":
	if qitem != None or item == None:
		me.SayTo(activator, "\nI can't ^teach^ you this now.")
	else:
		finish_quest()

		# Teach the skill
		activator.AcquireSkill(GetSkillNr("bow archery"), LEARN)

		# Create the player's first bow
		activator.CreateObjectInside("bow_short", 1, 1)
		activator.Write("Chereth gives you a short bow.", COLOR_WHITE)

		# Create some arrows for the player's bow
		activator.CreateObjectInside("arrow", 1, 12)
		activator.Write("Chereth gives you 12 arrows.", COLOR_WHITE)

# Teach sling archery
elif msg == "teach me sling":
	if qitem != None or item == None:
		me.SayTo(activator, "\nI can't ^teach^ you this now.")
	else:
		finish_quest()

		# Teach the skill
		activator.AcquireSkill(GetSkillNr("sling archery"), LEARN)

		# Create the player's first sling
		activator.CreateObjectInside("sling_small", 1, 1)
		activator.Write("Chereth gives you a small sling.", COLOR_WHITE)

		# Create some sling stones for the player's sling
		activator.CreateObjectInside("sstone", 1, 12)
		activator.Write("Chereth gives you 12 sling stones.", COLOR_WHITE)

# Teach crossbow archery
elif msg == "teach me crossbow":
	if qitem != None or item == None:
		me.SayTo(activator, "\nI can't ^teach^ you this now.")
	else:
		finish_quest()

		# Teach the skill
		activator.AcquireSkill(GetSkillNr("crossbow archery"), LEARN)

		# Create the player's first crossbow
		activator.CreateObjectInside("crossbow_small", 1, 1)
		activator.Write("Chereth gives you a small crossbow.", COLOR_WHITE)

		# Create some bolts for the player's crossbow
		activator.CreateObjectInside("bolt", 1, 12)
		activator.Write("Chereth gives you 12 bolts.", COLOR_WHITE)

# Give out links to the various archery skills
elif text[0] == "teach":
	if qitem != None:
		me.SayTo(activator, "\nSorry, I can only teach you |one| archery skill.")
	else:
		if item == None:
			me.SayTo(activator, "\nWhere is the ant queen's head? I don't see it.\nSolve the ^quest^ first and kill the ant queen.\nThen I will teach you.")
		else:
			me.SayTo(activator, "\nAs reward I will teach you an archery skill.\nChoose wisely. I can only teach you |one| of three archery skills.\nDo you want some information about the ^archery^ skills?\nIf you know your choice tell me ^teach me bow^,\n^teach me sling^ or ^teach me crossbow^.")

# Give out the quest information
elif text[0] == "quest":
	if qitem != None:
		me.SayTo(activator, "\nI have no quest for you after you helped us out.")
	else:
		if item == None:
			me.SayTo(activator, "\nYes, we need your help first.\nAs supply chief the water support of this outpost is under my command. We noticed last few days problems with our main water source.\nIt seems some giant ants have invaded the caverns under our water well.\nEnter the well next to this house and kill the ant queen!\nBring me her head as a proof and I will ^teach^ you.")
		else:
			me.SayTo(activator, "\nThe head! You have done it!\nNow we can repair the water well.\nSay ^teach^ to me now to learn an archery skill!")

# Greeting
elif msg == "hello" or msg == "hi" or msg == "hey":
	if qitem == None:
		if item == None:
			me.SayTo(activator, "\nHello, mercenary. I'm Supply Chief Chereth.\nFomerly Archery Commander Chereth, before I lost my eyes.\nWell, I still know a lot about ^archery^.\nPerhaps you want to ^learn^ an archery skill?")
		else:
			me.SayTo(activator, "\nThe head! You have done it!\nNow we can repair the water well.\nSay ^teach^ to me now to learn an archery skill!")
	else:
		me.SayTo(activator, "\nHello %s.\nGood to see you back.\nI have no quest for you or your ^archery^ skill." % activator.name)

# No valid message
else:
	activator.Write("%s listens to you without answer." % me.name, COLOR_WHITE)