<h1>Ad revenue dashboard</h1>

<h2>Description</h2>
Developed an advertising analytics tool as part of a group project in Java using Agile Scrum practices. The application helps companies evaluate the effectiveness of their advertising campaigns through an interactive dashboard that displays key performance metrics. These metrics assess campaign performance based on factors such as the number of users who visited or clicked on a page and the amount of time they spent on it. Users can visualize and compare these metrics through dynamic, side-by-side graphs for deeper insights.
<br />

<h2>Languages and Utilities Used</h2>

- <b>Java</b> 

<h2>Program Walkthrough:</h2>

To launch the application, run the **GUI** class located at: **ad-revenue-dashboard/src/main/java/main/dash/scene/GUI.java**

## 🔐 Login Page

When you first open the application, you’ll land on the **Login Page**, where you can either **sign in** to an existing account or **create a new one**.

### 🧾 Creating a New Account
1. Enter your desired **username** and **password** in the respective fields.  
2. Click **Create an Account**.  

Once your details are accepted, you’ll be taken directly to the **main dashboard**.

### 🔑 Logging In
- If you already have an account, simply enter your credentials and click **Login**.  
- If your username doesn’t match any existing account or your password is incorrect, an error dialog will appear: **“User does not exist, or password is incorrect.”**
- Click **OK** to dismiss the message and return to the login screen.
<p align="center">
<img src="https://live.staticflickr.com/65535/54836986886_e4756e0242_w.jpg" height="80%" width="80%"/>

<p align="center">
<img src="https://live.staticflickr.com/65535/54836992861_6d977ae51c_z.jpg" height="80%" width="80%"/>

## 📊 Main Dashboard

After logging in, the **Dashboard Screen** displays **three side-by-side graphs** of key metrics.  
Above each graph, you’ll see the **current value** of that metric for the selected time period.  
Below each graph are **filters for Gender, Age, and Income** — use these to narrow down the data shown.

On the right, three **dropdown menus** let you choose which metrics to display in each graph.  
If you select **Bounce Rate** as one of your metrics, two additional prompt boxes will appear:

- **Time Threshold:** Define the minimum time on page (e.g., *30 seconds*) before a visit is considered a bounce.  
- **Page-View Threshold:** Define the maximum number of pages viewed (e.g., *1*) before a visit is considered a bounce.  

If you want to **compare two metrics on the same axes** for the first graph, click the **Overlay Metric** button —  
the second metric will layer over the first for easier comparison.

Below these controls is the **Time Granularity** dropdown.  
Use it to view your graphs by **Day**, **Hour**, or **Month**, depending on how detailed you want your analysis to be.

<p align="center">
<img src="https://live.staticflickr.com/65535/54837246108_7906d62e32_c.jpg" height="80%" width="80%"/>

## 🗂️ File Menu

At the top left of the main dashboard, the **File** menu provides key data-management options:

- **Import:** Upload new impression, click, or log files to use as input data.  
- **Export:** Save the current set of charts in either **CSV** or **PDF** format.  
- **Exit:** Close the application. When you choose Exit, a confirmation prompt appears — click **OK** to quit or **Cancel** to stay.

---

## 👥 User Operations & Management

Next to **File** is the **User Operations** menu, where you can **export a record of all user actions** (such as logins and exports) in **CSV** or **PDF** format.

Click **Manage Users** at the top of the dashboard, then select **User Manager** to open the user administration panel.  
From here, you can:
- Change individual **user roles**.  
- **Remove users** from the database entirely.

<p align="center">
<img src="https://live.staticflickr.com/65535/54836135412_0254bda5f5_z.jpg" height="80%" width="80%"/>

## ❓ Help Document and Settings

If you ever need to view this guidance while on the main dashboard, click the **Help Document** button at the top of the dashboard, then select **Manual** to revisit this guide.

If you want to modify the application’s configuration, click **Settings** from within the Help tab to adjust your preferences.

<p align="center">
<img src="https://live.staticflickr.com/65535/54837325265_ff30678983.jpg" height="80%" width="80%"/>


<!--
 ```diff
- text in red
+ text in green
! text in orange
# text in gray
@@ text in purple (and bold)@@
```
--!>
