/************************************************************************
*            Atrinik, a Multiplayer Online Role Playing Game            *
*                                                                       *
*                     Copyright (C) 2009 Alex Tokar                     *
*                                                                       *
* Fork from Daimonin (Massive Multiplayer Online Role Playing Game)     *
* and Crossfire (Multiplayer game for X-windows).                       *
*                                                                       *
* This program is free software; you can redistribute it and/or modify  *
* it under the terms of the GNU General Public License as published by  *
* the Free Software Foundation; either version 2 of the License, or     *
* (at your option) any later version.                                   *
*                                                                       *
* This program is distributed in the hope that it will be useful,       *
* but WITHOUT ANY WARRANTY; without even the implied warranty of        *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
* GNU General Public License for more details.                          *
*                                                                       *
* You should have received a copy of the GNU General Public License     *
* along with this program; if not, write to the Free Software           *
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.             *
*                                                                       *
* The author can be reached at admin@atrinik.org                        *
************************************************************************/

/* altar.c */
extern int apply_altar(object *altar, object *sacrifice, object *originator);

/* armour_improver.c */
extern void apply_armour_improver(object *op, object *tmp);

/* arrow.c */
extern object *fix_stopped_arrow(object *op);
extern void move_arrow(object *op);

/* book.c */
extern void apply_book(object *op, object *tmp);

/* container.c */
extern int esrv_apply_container(object *op, object *sack);
extern int container_link(player *pl, object *sack);
extern int container_unlink(player *pl, object *sack);
extern int container_trap(object *op, object *container);

/* converter.c */
extern int convert_item(object *item, object *converter);

/* creator.c */
extern void move_creator(object *op);

/* deep_swamp.c */
extern void walk_on_deep_swamp(object *op, object *victim);
extern void move_deep_swamp(object *op);

/* detector.c */
extern void move_detector(object *op);

/* disease.c */
extern int move_disease(object *disease);
extern int infect_object(object *victim, object *disease, int force);
extern void move_symptom(object *symptom);
extern void check_physically_infect(object *victim, object *hitter);
extern int cure_disease(object *sufferer, object *caster);
extern int reduce_symptoms(object *sufferer, int reduction);

/* door.c */
extern object *find_key(object *op, object *door);
extern int open_door(object *op, mapstruct *m, int x, int y, int mode);
extern void remove_door(object *op);
extern void open_locked_door(object *op, object *opener);
extern void close_locked_door(object *op);

/* food.c */
extern void apply_food(object *op, object *tmp);
extern void create_food_force(object *who, object *food, object *force);
extern void eat_special_food(object *who, object *food);
extern int dragon_eat_flesh(object *op, object *meal);

/* gate.c */
extern void move_gate(object *op);
extern void move_timed_gate(object *op);

/* gravestone.c */
extern char *gravestone_text(object *op);

/* identify_altar.c */
extern int apply_identify_altar(object *money, object *altar, object *pl);

/* light.c */
extern void apply_player_light_refill(object *who, object *op);
extern void apply_player_light(object *who, object *op);
extern void apply_lighter(object *who, object *lighter);

/* marker.c */
extern void move_marker(object *op);

/* monster.c */
extern void set_npc_enemy(object *npc, object *enemy, rv_vector *rv);
extern object *check_enemy(object *npc, rv_vector *rv);
extern object *find_enemy(object *npc, rv_vector *rv);
extern int can_detect_target(object *op, object *target, int range, int srange, rv_vector *rv);
extern int can_detect_enemy(object *op, object *enemy, rv_vector *rv);
extern int move_monster(object *op);
extern object *find_nearest_living_creature(object *npc);
extern int move_randomly(object *op);
extern int can_hit(object *ob1, object *ob2, rv_vector *rv);
extern int can_apply(object *who, object *item);
extern object *monster_choose_random_spell(object *monster);
extern int monster_cast_spell(object *head, object *part, object *pl, int dir, rv_vector *rv);
extern int monster_use_skill(object *head, object *part, object *pl, int dir);
extern int monster_use_wand(object *head, object *part, object *pl, int dir);
extern int monster_use_rod(object *head, object *part, object *pl, int dir);
extern int monster_use_horn(object *head, object *part, object *pl, int dir);
extern int monster_use_bow(object *head, object *part, object *pl, int dir);
extern int check_good_weapon(object *who, object *item);
extern int check_good_armour(object *who, object *item);
extern void monster_check_pickup(object *monster);
extern int monster_can_pick(object *monster, object *item);
extern void monster_apply_below(object *monster);
extern void monster_check_apply(object *mon, object *item);
extern void npc_call_help(object *op);
extern int dist_att(int dir, object *ob, object *enemy, object *part, rv_vector *rv);
extern int run_att(int dir, object *ob, object *enemy, object *part, rv_vector *rv);
extern int hitrun_att(int dir, object *ob, object *enemy);
extern int wait_att(int dir, object *ob, object *enemy, object *part, rv_vector *rv);
extern int disthit_att(int dir, object *ob, object *enemy, object *part, rv_vector *rv);
extern int wait_att2(int dir, object *ob, object *enemy, object *part, rv_vector *rv);
extern void circ1_move(object *ob);
extern void circ2_move(object *ob);
extern void pace_movev(object *ob);
extern void pace_moveh(object *ob);
extern void pace2_movev(object *ob);
extern void pace2_moveh(object *ob);
extern void rand_move(object *ob);
extern void communicate(object *op, char *txt);
extern int talk_to_npc(object *op, object *npc, char *txt);
extern int talk_to_wall(object *npc, char *txt);
extern object *find_mon_throw_ob(object *op);
extern int monster_use_scroll(object *head, object *part, object *pl, int dir, rv_vector *rv);
extern int is_friend_of(object *op, object *obj);

/* pit.c */
extern void move_pit(object *op);

/* player.c */
extern player *find_player(char *plname);
extern void display_motd(object *op);
extern int playername_ok(char *cp);
extern int add_player(NewSocket *ns);
extern archetype *get_player_archetype(archetype *at);
extern object *get_nearest_player(object *mon);
extern int path_to_player(object *mon, object *pl, int mindiff);
extern void give_initial_items(object *pl, treasurelist *items);
extern void get_name(object *op);
extern void get_password(object *op);
extern void confirm_password(object *op);
extern object *find_arrow(object *op, const char *type);
extern void fire(object *op, int dir);
extern int move_player(object *op, int dir);
extern int handle_newcs_player(player *pl);
extern int save_life(object *op);
extern void remove_unpaid_objects(object *op, object *env);
extern void do_some_living(object *op);
extern void kill_player(object *op);
extern void fix_weight(void);
extern void cast_dust(object *op, object *throw_ob, int dir);
extern void make_visible(object *op);
extern int is_true_undead(object *op);
extern int hideability(object *ob);
extern void do_hidden_move(object *op);
extern int stand_near_hostile(object *who);
extern int action_makes_visible(object *op);
extern int pvp_area(object *attacker, object *victim);
extern void dragon_ability_gain(object *who, int atnr, int level);

/* player_mover.c */
extern void move_player_mover(object *op);

/* poison.c */
extern void apply_poison(object *op, object *tmp);
extern void poison_more(object *op);

/* potion.c */
extern int apply_potion(object *op, object *tmp);

/* power_crystal.c */
extern void apply_power_crystal(object *op, object *crystal);

/* rod.c */
extern void regenerate_rod(object *rod);

/* savebed.c */
extern void apply_savebed(object *op);

/* scroll.c */
extern void apply_scroll(object *op, object *tmp);

/* shop_mat.c */
extern int apply_shop_mat(object *shop_mat, object *op);

/* sign.c */
extern void apply_sign(object *op, object *sign);

/* skillscroll.c */
extern void apply_skillscroll(object *op, object *tmp);

/* spawn_point.c */
extern void spawn_point(object *op);

/* spellbook.c */
extern void apply_spellbook(object *op, object *tmp);

/* teleporter.c */
extern void move_teleporter(object *op);

/* treasure.c */
extern void apply_treasure(object *op, object *tmp);

/* waypoint.c */
extern object *get_active_waypoint(object *op);
extern object *get_aggro_waypoint(object *op);
extern object *get_return_waypoint(object *op);
extern object *find_waypoint(object *op, const char *name);
extern void waypoint_compute_path(object *waypoint);
extern void waypoint_move(object *op, object *waypoint);

/* weapon_improver.c */
extern int check_weapon_power(object *who, int improvs);
extern void apply_weapon_improver(object *op, object *tmp);