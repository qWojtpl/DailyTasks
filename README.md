<p align="center">
    <img src="images/logo.png">
</p>

# DailyTasks

<p>Add daily tasks to your Minecraft server</p>
<p>Plugin every day randomizes 3 task to complete - if player will complete all tasks in month, then player will get special month reward</p>
<p>Tested Minecraft versions: </p>

`1.18.2` `1.19.3`

# Installation

<p>Put dailytasks.jar from https://github.com/qWojtpl/dailytasks/releases/latest to your plugins folder and restart the server.</p>

# Supported events

```java
- join        // Join event, eg. join 1 server
- kill        // Kill event, eg. kill 20 zombie
- break       // Break block event, eg. break 64 dirt
- place       // Place block event, eg. place 128 spruce_log
- pickup      // Pickup (how many items, not how many times) item event, eg. pickup 32 slime_ball
- T_pickup    // Pickup (how many times, not how many items) item event, eg. T_pickup 5 dirt
- drop        // Drop (how many items, not how many times) item event, eg. drop 64 stone
- T_drop      // Drop (how many times, not how many items) item event, eg. T_drop 10 diamond_sword
- craft       // Craft (how many items, not how many times) item event, eg. craft 1 cake
- T_craft     // Craft (how many times, not how many items) item event, eg. craft 10 diamond_pickaxe
- enchant     // Enchant item event, eg. enchant 1 diamond_sword
- fish        // Fish (using fishing rod) event, eg. fish 64 pufferfish
- catch       // Catch (using fishing rod) entity, eg. catch 10 wolf
- shoot       // Shoot event, eg. shoot 20 bow
- throw       // Throw event, eg. throw 64 snowball, throw 10 trident
- command     // Send command event (without arguments), eg. command 30 /ae
- chat        // Send chat message event, eg. chat 10 Wiggle-Wiggle
- breed       // Breed animals event, eg. breed 10 cow
- sign        // Edit sign event, eg. sign 10 This_is_my_house
- furnace     // Take from furnace event, eg. furnace 15 glass (glass is a product)
- eat         // Eat event, eg. eat 64 apple
```

# Configuration

<details><summary>config.yml</summary>

`deleteOldData` - When set to true old data (from previous or older month) will be deleted from local file<br>
`saveInterval` - Set interval to save all files automatically<br>
`logSave` - When set to true then information on save will be printed to console<br>

## Default configuration:

```yml
config:
    deleteOldData: false
    saveInterval: 300
    logSave: true
```

</details>


<details><summary>task-pool.yml</summary>

<br>

**You need at least 3 tasks in task pool to start this plugin**<br> 
`Use %rdm% for random number`<br>
`* means anything, eg. kill 10 * means kill 10 of any entity`<br>

`enabled` - If set to true task is enabled and can be loaded into task pool<br>
`event` - What player need to do to complete this task<br>
`numberMin` - Min number for random number (%rdm%)<br>
`numberMax` - Max number for random number (%rdm%)<br>

## Default configuration:

```yml
tasks:
  "0":
    enabled: true
    event: "kill %rdm% villager"
    numberMin: 1
    numberMax: 10
  "1":
    enabled: true
    event: "kill %rdm% spider"
    numberMin: 10
    numberMax: 30
  "2":
    enabled: true
    event: "break %rdm% *"
    numberMin: 30
    numberMax: 60
```

</details>

<details><summary>reward-pool.yml</summary>

<br>

**You need at least 1 day reward and 1 month reward in reward pool to start this plugin**<br> 
`Use %rdm% for random number`<br>
`Use %player% for player`<br>

`enabled` - If set to true reward is enabled and can be loaded into reward pool<br>
`command` - This command is executing by console when player is getting reward<br>
`numberMin` - Min number for random number (%rdm%)<br>
`numberMax` - Max number for random number (%rdm%)<br>

## Default configuration:

```yml
day-rewards:
  "0":
    enabled: true
    command: "give %player% minecraft:emerald %rdm%"
    numberMin: 10
    numberMax: 15

month-rewards:
  "0":
    enabled: true
    command: "give %player% minecraft:diamond_block %rdm%"
    numberMin: 10
    numberMax: 32
```

</details>

<details><summary>pluginData.yml</summary>

**Manually modifying this file could cause plugin crashes!**<br>
**DO NOT EDIT IT IF YOU DON'T KNOW WHAT ARE YOU DOING!**<br>

## data:

`lastRandomized` - Specifies last date when tasks and rewards was randomized. If this date doesn't equals actual date, the new tasks (and rewards) will be randomized<br>
`fakeCalendar` - Fakecalendar info. If you're using fake calendar then actual fakecalendar date will be saved in this field<br>

```yml
data:
  lastRandomized: 2023/3/23
  fakeCalendar: 2023 3 23 0 16 06
```

## history:

`#date` - Certain date contains list of 3 items (tasks) which have to been completed in this date. You can modify them, doesn't depend on task pool<br>

```yml
history:
  2023/3/22:
  - break 10 obsidian
  - kill 12 zombie
  - breed 5 cow
  2023/3/23:
  - place 128 *
  - eat 32 apple
  - fish 5 cod
```

## day-reward-history:

`#date` - Date is a key, value contains command which will be executed when player complete all tasks in this day. Can be modified, doesn't depend on reward pool<br>

```yml
day-reward-history:
  2023/3/22: give %player% emerald 16
  2023/3/23: give %player% diamond 32
  2023/3/24: say %player% completed today's tasks!
```

## month-reward-history

`#month` - Year/month is a key, value contains command which will be executed when player complete all tasks in this month. Can be modified, doesn't depend on reward pool<br>

```yml
month-reward-history:
  2023/2: give %player% diamond_block 32
  2023/3: give %player% netherite_ingot 48
  2023/4: give %player% emerald_block 64
```

</details>

# Commands & Permissions

`/dt` - Shows this month tasks, permission to complete any daily task `dt.use`<br>
`/dt help` - Shows help page, this permission let to use sub-commands of /dt `dt.manage`<br>
`/dt reload` - Reload configuration. Do not use when you're changing SQL info `dt.reload`<br>
`/dt fakecalendar` - Create fake calendar `dt.fakecalendar`<br>
`/dt removefake` - Remove fake calendar `dt.removefake` <br>
`/dt autocomplete` - Mark date as auto-complete `dt.setautocomplete`<br>
`/dt checkauto` - Check if date is marked as auto-complete `dt.checkauto`<br>
`/dt complete` - Complete (day/date/ptask index for this day) for player `dt.complete` (`dt.complete.day`, `dt.complete.date`, `dt.complete.task`)<br>
`/dt checkcomplete` - Check if player has completed (day/date/task index) `dt.checkcomplete` (`dt.checkcomplete.day`, `dt.checkcomplete.date`, `dt.checkcomplete.task`)<br>
`/dt checktasks` - Check tasks for specified date (if available) `dt.checktasks`<br>
`/dt checkrewards` - Check rewards for specified date (if available) `dt.checkrewards`<br>
`/dt taskpool` - Shows task pool `dt.taskpool`<br>
`/dt rewardpool` - Shows reward pool `dt.rewardpool`<br>
`/dt reserve` - Reserve task/daily reward/monthly reward `dt.reserve` (`dt.reserve.task`, `dt.reserve.reward`, `dt.reserve.reward.day`, `dt.reserve.reward.month`)<br>
`/dt add` - Add task/daily reward/monthly reward to pool `dt.add` (`dt.add.task`, `dt.add.reward`, `dt.add.reward.day`, `dt.add.reward.month`)
