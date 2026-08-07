# Frontend Application (React + Vite)

React Single Page Application built with Vite.

## Folder Architecture (`src/`)

- **`public/`**: Static assets served as-is (favicons, robots.txt, public icons).
- **`src/assets/`**: Images, SVGs, and static media referenced inside React components.
- **`src/components/`**: Reusable UI components (buttons, input fields, navigation bar, resume uploader, score cards).
- **`src/pages/`**: View components corresponding to application routes (e.g. Dashboard, Resume Analysis view, History, Settings).
- **`src/services/`**: API client modules (Axios/Fetch HTTP instances, endpoint services for communicating with the Spring Boot API).
- **`src/hooks/`**: Custom React hooks (e.g., `useAuth`, `useResumeUpload`, `useAnalysisResult`).
- **`src/context/`**: React Context providers (e.g., AuthContext, ThemeContext, ApplicationState).
- **`src/utils/`**: Helper utilities, formatting logic (dates, file size formatters), and constants.
- **`src/styles/`**: Global styles, CSS modules, design tokens, and theme definitions.
