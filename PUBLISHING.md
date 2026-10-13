# Publishing Meal Sheet on Google Play

Everything the app needs is in this folder. The steps marked **(you)** need your identity, payment, or a decision only you can make. Everything else is already done.

## What's ready

| File | What it is |
|---|---|
| `MealSheet-1.0.0.aab` | The app bundle you upload to Play Console |
| `fastlane/metadata/android/en-US/title.txt` | Store title (29/30 chars) |
| `fastlane/metadata/android/en-US/short_description.txt` | Short description (80/80 chars) |
| `fastlane/metadata/android/en-US/full_description.txt` | Full description |
| `fastlane/metadata/android/en-US/images/icon.png` | 512×512 store icon |
| `fastlane/metadata/android/en-US/images/featureGraphic.png` | 1024×500 feature graphic |
| `fastlane/metadata/android/en-US/images/phoneScreenshots/1–5.png` | Phone screenshots, in order |
| `PRIVACY.md` | Privacy policy (fill in your name and email, then put it online) |
| `meal-sheet-upload.jks` + `keystore.properties` | **Your upload key.** See below. |

### Back up your upload key (you)

Copy `meal-sheet-upload.jks` **and** `keystore.properties` somewhere safe and private, like a USB stick or a private folder in your Google Drive. Every future update must be signed with this key. If it's lost, Google can reset it, but that takes a support request and a few days. Never share these files or put them on GitHub. The `.gitignore` already keeps them out.

---

## Step 1: Create your developer account (you, ~20 min, plus verification wait)

1. Make a **new Gmail address just for this**, like `mealsheetapp@gmail.com`. Google shows the developer email publicly on the store page.
2. Go to **play.google.com/console/signup** and choose **"Yourself"** (personal account).
3. Pay the **$25** one-time fee, then verify your identity with a government ID and confirm your phone and email. Verification can take a few days.
4. Pick a **developer name**. It can be your name or something like "Meal Sheet." You can change it later.

**What's public on a free app:** your legal name, your country, and the developer email. Your full address is only shown if you charge money for something.

## Step 2: Put the privacy policy online (you)

Google requires a public link, even for apps that collect nothing.

1. In `PRIVACY.md`, replace `[YOUR NAME]` and `[YOUR DEVELOPER EMAIL]`.
2. Pick one:
   - **GitHub (recommended, and it makes the app open source):** create a free GitHub account, then a public repository named `meal-sheet`. Claude can upload the project for you. Your link will be `https://github.com/<username>/meal-sheet/blob/main/PRIVACY.md`.
   - **Google Sites (no GitHub needed):** sign in with the new Gmail, create a site, paste the policy text, publish, and copy the link.

## Step 3: Create the app in Play Console (you, ~45 min; answers below)

**Create app:** App name `Meal Sheet: Work Meal Tracker` · Default language English (United States) · **App** · **Free** · accept the declarations.

On the Dashboard, work through **"Set up your app"**:

| Section | Answer |
|---|---|
| Privacy policy | Your link from Step 2 |
| App access | All functionality is available without special access |
| Ads | No, my app does not contain ads |
| Content rating | Email: your developer email. Category: **"All other app types"**. Answer **No** to every question (violence, sexual content, language, drugs, gambling, user interaction/sharing, location, purchases). Expected rating: Everyone / PEGI 3. |
| Target audience | **18 and over** (you may also tick 16–17 if teen restaurant workers are in mind). "Could the app appeal to children?" **No** |
| News app | No |
| Data safety | "Does your app collect or share any of the required user data types?" **No**. That's the whole form. The listing will show *"No data collected · No data shared."* |
| Government app | No |
| Financial features | My app doesn't provide any financial features |
| Health apps | My app does not have any health features |
| Advertising ID | No (the app doesn't use it) |

**Main store listing:** paste in `title.txt`, `short_description.txt`, and `full_description.txt`. Upload `icon.png`, `featureGraphic.png`, and the 5 screenshots in order. Set **Category: Productivity** and contact email: your developer email.

## Step 4: Closed test with 12+ testers for 14 days (you + coworkers)

This is the longest step. New personal accounts must have **at least 12 testers opted in continuously for 14 days** before going public. Google also checks that testers actually used the app.

1. **Recruit 14–15 people** so you have a buffer if someone drops out. They need an **Android phone** and a **Gmail/Google account**. iPhone users can't test.
2. Play Console → **Test and release → Testing → Closed testing** → create a track → **Testers** tab → create an email list with their Google account emails → Save.
3. **Create release** → upload `MealSheet-1.0.0.aab` → accept **Play App Signing** (the default) → release notes: "First release." → **Review** → **Start rollout**. Google reviews it, which takes from a few hours to a few days.
4. Copy the **opt-in link** from the Testers tab and send it to everyone. Each tester opens it **while signed in to the Gmail you listed**, taps **"Become a tester,"** then installs from the Play Store link.
   - **Anyone with the sideloaded version (including you) must uninstall it first.** The Play version is signed differently and won't install over it.
5. Tell testers: **stay opted in for at least 14 days, and use it a few times a week.** Ask them what they think, because Google asks for this feedback later.

## Step 5: Apply for production (you, ~15 min, then up to ~7 days)

After 14 days: **Dashboard → Apply for production.** Google asks three sets of questions:

- **About your closed test:** how you recruited testers (coworkers who get charged for meals at work), whether they used every feature, and a summary of their feedback.
- **About your app:** audience (hourly workers charged for workplace meals), value (a private personal record to check paycheck meal deductions), and expected first-year installs (be honest; "0–1,000" is fine).
- **About production readiness:** what you changed based on feedback, and why you think it's ready.

Click **Next** on each section, then **Apply** on the last one, or your answers are lost. Review usually takes **7 days or less**. Once approved: **Production → Create release** → add the same bundle → choose countries → roll out.

---

## Updating the app later

1. In `app/build.gradle.kts`, raise `versionCode` (2, 3, …) and `versionName`.
2. Run `gradlew bundleRelease` (or ask Claude to). The new bundle is at `app/build/outputs/bundle/release/app-release.aab`.
3. Upload it as a new release.

Google raises the required Android version every year around **August 31**, so plan on at least one small update per year.

## Optional: F-Droid (free, open-source app store)

[F-Droid](https://f-droid.org) has no fee and no tester requirement, but the source code has to be public under an open-source license. The store text and images are already in the `fastlane/` layout that F-Droid reads. Once the code is on GitHub with a license, you'd submit a request at gitlab.com/fdroid/rfp.

## License

Before the code goes public, pick a license. On GitHub: **Add file → Create new file**, name it `LICENSE`, then click **Choose a license template**.

- **GPL-3.0** keeps any copies or changes open source too.
- **MIT** is shorter and lets anyone do anything with the code.

Either one is fine for F-Droid.
