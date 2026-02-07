# Yurushi

Discord Integration Whitelist Mod for Fabric. Simplify your server access management.

## Features

- **Discord Integration:** Players request whitelist access via a Discord form (Modal).
- **One-Click Approval:** Admins can approve or deny requests directly from Discord with a single button click.
- **Automated Communication:** 
  - Sends a DM to the player upon approval or denial.
  - Updates the original request message to reflect the decision.
- **Customizable Messages:** Fully configurable text and embed styles via `messages.toml`.
- **Role-Based Access:** Restrict whitelist management to specific Discord roles using `whitelistRole`.
- **Debug Tools:** Visualize your embed configurations with `/debug embeds`.

## Config

Configuration files are located in `config/Yurushi/`.

### Yurushi.toml

Main plugin configuration.

| Config | Type | Description |
|--------|------|-------------|
| `botToken` | String | Your Discord Bot Token |
| `adminChannelId` | String | ID of the Discord text channel where requests are sent |
| `whitelistRole` | List | List of Role IDs allowed to approve/deny requests |

<details>
  <summary>Yurushi.toml</summary>

  ```toml
  botToken = "YOUR_BOT_TOKEN_HERE"
  adminChannelId = "123456789012345678"
  whitelistRole = ["987654321098765432", "112233445566778899"]
  ```

</details>

### messages.toml

Customize all bot messages, embeds, and button labels. Supports placeholders like `{minecraft_username}`, `{reason}`, etc. *(Might not available in all messages).*

<details>
  <summary>messages.toml</summary>

  ```toml
  [error]
  no_permission = "❌ You don't have permission to perform this action."
  server_unavailable = "❌ Server is not available. Please try again later."
  unexpected = "❌ An unexpected error occurred."
  admin_channel_not_found = "An error occurred. Please contact an administrator."

  [button]
  [button.approve]
  label = "Approve"
  success = "✅ Whitelist request for `{minecraft_username}` has been approved!\n**UUID:** `{uuid}`"
  already_whitelisted = "⚠️ `{minecraft_username}` is already whitelisted on the server."
  player_not_found = "❌ Player `{minecraft_username}` was not found on Mojang's servers.\nThis username might be incorrect or doesn't exist."
  failed = "❌ Failed to whitelist `{minecraft_username}`.\nError: {error}"

  [button.deny]
  label = "Deny"
  success = "Whitelist request for `{minecraft_username}` has been denied.\n**Reason:** {reason}"

  [embed]
  [embed.request]
  title = "🕚 Whitelist Request"
  footer = "User ID: {user_id}"
  [embed.request.fields]
  discord_user = "Discord User"
  minecraft_username = "Minecraft Username"
  description = "Description"
  description_empty = "*No description provided*"

  [embed.approved]
  title = "✅ Whitelist Request - Approved"
  footer = "You can now join the server!"
  [embed.approved.fields]
  approved_by = "Approved By"

  [embed.already_whitelisted]
  title = "⚠️ Whitelist Request - Already Whitelisted"
  footer = "This player is already whitelisted."

  [embed.denied]
  title = "❌ Whitelist Request - Denied"
  footer = "Please contact an administrator if you have questions."
  [embed.denied.fields]
  reason = "Reason"
  denied_by = "Denied By"

  [dm]
  [dm.approved]
  title = "✅ Whitelist Request Approved"
  description = "Congratulations! Your whitelist request has been approved."

  [dm.denied]
  title = "❌ Whitelist Request Denied"
  description = "Unfortunately, your whitelist request has been denied."

  [modal]
  [modal.register]
  title = "Whitelist Registration"
  success = "Your whitelist request has been submitted!"
  [modal.register.inputs]
  username_label = "Minecraft Username"
  username_placeholder = "Enter your Minecraft username"
  description_label = "Description (Optional)"
  description_placeholder = "Tell us a bit about yourself or why you want to join..."

  [modal.deny]
  title = "Deny Whitelist Request"
  [modal.deny.inputs]
  reason_label = "Denial Reason"
  reason_placeholder = "Provide a reason for denying the whitelist request..."
  reason_empty = "*No reason provided*"
  ```

</details>

## How to Use

1. Download the mod jar and place it in your server's `mods` folder.
2. Start the server once to generate config files.
3. Configure `Yurushi.toml` with your bot details.
4. Restart the server.
5. Use the provided Discord integration to manage whitelist requests.

## Bug Reports & Suggestions

If you encounter any bugs or have suggestions for improvements, please open an issue on the [GitHub Issues](https://github.com/tantaihaha4487/Yurushi/issues) page.
