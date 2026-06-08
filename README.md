# Description

Web Snapshot is an elegant plugin that gives you browser-accurate snapshots with just one click. Perfect for creating professional reports, archiving data, or sharing information.

# Features

* **One-Click Downloads** - Easily convert Joget forms and userviews into a PDF, PNG, WEBP or JPEG
* **Batch Processing** - Download multiple forms in DataList at once and get them packaged in a ZIP file
* **Browser-Accurate Rendering** - Captures exactly as what you see on screen, with formatting and styling
* **Custom File Names** - Name your snapshot files whatever makes sense for you
* **Seamless Integration** - Works directly within your Joget without complicated setup
* **Session-Aware** - Maintains your user context and permissions for secure downloads

# Installation

To use Web Snapshot, you first need to get the plugin installed in Joget:

## Step 1: Download the Plugin

Get the Web Snapshot plugin JAR file from the releases page.

## Step 2: Add to Joget

1. Log into Joget with admin access
2. Go to **Admin > Manage Plugins**
3. Click **Choose File**
4. Choose the Web Snapshot JAR file
5. Click **Upload**

## Step 3: Verify Installation

You should see "Web Snapshot" listed in your plugins. You're ready to go!

> **Note:** You need Joget 8.0 or newer for this plugin to work.

# Quick Start

Now that Web Snapshot is installed, you need to set up Gotenberg (the service that creates PDFs and images). Don't worry, it's easier than it sounds!

## Step 1: Start Gotenberg

The `compose.yaml` file in this project has everything you need to start Gotenberg quickly.

**If you have Docker installed:**

1. Open a terminal and go to the `gotenberg` folder in this project
2. Run this command:
   ```
   docker-compose up
   ```
3. It's ready to use once you see something like **"[SYSTEM] api: server started on [::]:3000"**

> **Notes:** Leave this terminal window open while you use Web Snapshot. When you're done, press `Ctrl+C` to stop it.

> **Pro Tips:** Use `docker compose up -d` to keep it running in background. Then `docker compose down -v` to shut it down.

**If you don't have Docker:**

You can install Docker Desktop from [docker.com](https://www.docker.com/products/docker-desktop) or ask your IT team for help.

## Step 2: Connect Joget to Gotenberg

1. In Joget, go to **Admin > Settings**
2. Look for **Gotenberg Server** settings
3. Review the default settings:
   - **Scheme:** `http`
   - **Domain:** `gotenberg.local`
   - **Port:** `3000`
4. Click **Test Connection** — you should see a success message

**If the connection fails:**

- Make sure the terminal from Step 1 is still running
- Check that your firewall allows communication on port 3000
- If the hostname doesn't work, try using `localhost` or identify the container's IP address

5. Choose your format:
   - **PDF** — Best for documents, reports, and official records
   - **Image** — Best for web pages, forms, and screenshots

> **Pro Tips:** Fine tune the screen size, paper size, margins, scale, etc to get accurate results

**Done!** Your Web Snapshot is ready to use.

# Using Web Snapshot

## Basic Workflow

Here's how to use Web Snapshot in your day-to-day work:

### Download Snapshot For A Userview

1. Make sure to **Publish** the App
1. Navigate into UI Builder
3. Choose the menu then click on **Edit Page Components** button
4. Drag **PageSnap** into builder canvas
5. Click **Done Editing Page Components**
6. Click **Save** button
7. Click **Launch** button
8. Navigate to the userview menu which contains the newly added **PageSnap** button
9. Click on the **PageSnap** button to download a snapshot

### Download Multiple Form Snapshots From A DataList

1. In List Builder, drag **FormSnap** into builder canvas
2. Click **Save** button
3. Navigate to the userview menu which contains the newly added **FormSnap** button
4. Tick multiple rows in your DataList
5. Click **FormSnap** button to download snapshot for selected rows

> **Warning:** It's not recommended to download large number of snapshots from a DataList. This will most likely consume high CPU/RAM/time for Joget/Gotenberg to complete which then may affect other users.

### File Names

When you download with **Custom File Name:**, you'll get: `MyFileName-{timestamp}.pdf`

# Known Limitations on Gotenberg

* **Fonts** - might need to install additional fonts based on the web page you're trying to snapshot
* **Chrome Print** - Snapshot results might slightly differ versus printed PDF on your Chrome

# Getting Help

JogetOSS is a community-led team for open source software related to the [Joget](https://www.joget.org) no-code/low-code application platform.
Projects under JogetOSS are community-driven and community-supported.
To obtain support, ask questions, get answers and help others, please participate in the [Community Q&A](https://answers.joget.org/).

# Contributing

This project welcomes contributions and suggestions, please open an issue or create a pull request.

Please note that all interactions fall under our [Code of Conduct](https://github.com/jogetoss/repo-template/blob/main/CODE_OF_CONDUCT.md).

# Licensing

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

NOTE: This software may depend on other packages that may be licensed under different open source licenses.
