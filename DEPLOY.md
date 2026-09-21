# Deploying to Get a Live Link (for LinkedIn)

I can't deploy this to a live server on your behalf (no internet access on my end),
but here's the fastest free way to get a public link yourself — takes about 10-15 minutes.

## Option A: Render.com (Recommended, free tier, easiest)

1. Push this `loanrisk` folder to a **GitHub repository** (create one at github.com if you don't have it,
   then `git init`, `git add .`, `git commit -m "loan risk project"`, `git push`).
2. Go to https://render.com → Sign up (free) → **New +** → **Web Service**.
3. Connect your GitHub repo.
4. Render will detect the `Dockerfile` automatically — just click **Create Web Service**.
5. Wait 3-5 minutes for the build. Render will give you a live URL like:
   `https://loanrisk-xxxx.onrender.com`
6. Open it, log in with `admin` / `admin123`, and that's your live link for LinkedIn.

**Note:** Free Render services "sleep" after 15 minutes of no traffic and take ~30 seconds to
wake up on the next visit — totally fine for a portfolio/demo link.

## Option B: Railway.app (Also free tier, similar steps)

1. Push code to GitHub (same as above).
2. Go to https://railway.app → New Project → Deploy from GitHub repo.
3. Railway auto-detects the Dockerfile and deploys.
4. Under Settings → Networking, click **Generate Domain** to get your public URL.

## Before You Deploy — Do This

1. **Change the admin password** in `SecurityConfig.java` (don't leave `admin123` on a public link).
2. **Add your Gemini API key** in `application.properties` (`gemini.api.key=...`) —
   get a free one at https://aistudio.google.com/app/apikey
3. If you want real emails to send, add your Gmail credentials in `application.properties` too.
   (If you skip this, the app still works fine — it just logs "email failed" quietly.)

## What to Write in Your LinkedIn Post

> Built an "Early Warning System for Loan Default Risk" — a Java Spring Boot app that
> flags loan customers as Low/Medium/High risk using a transparent, rule-based scoring
> engine (fully auditable, no black-box ML), with Gemini API generating natural-language
> risk explanations and personalized customer reminder emails.
> 🔗 Live demo: [your Render link]
> 🛠️ Java · Spring Boot · Spring Security · JPA · Gemini API · H2

Attach a screenshot of the dashboard — posts with an image get much better reach.
