<p align="center">
    <img src="https://media.discordapp.net/attachments/816647374239694849/1082077755451125811/57909dd196ba9e180fda889a79e662f468e299abbe166ffe875bf59d7425202e88937ffb57ad5c879dbd77fbaa4992b20175f2f8a6faff19ec765d2980de3079d0fcf6ec45967565d9fab2ff.png">
</p>

# DailyTasks

<p>Add daily tasks to your Minecraft server</p>
<p>Tested Minecraft versions: </p>

`Spigot 1.18.2`

# Installation

<p>Put dailytasks.jar from https://github.com/qWojtpl/dailytasks/releases/latest to your plugins folder and restart the server.</p>

# Configuration

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