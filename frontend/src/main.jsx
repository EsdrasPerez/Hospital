import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AuthProvider } from './auth/AuthProvider.jsx'
// Bootstrap 4 + Now UI Dashboard (Creative Tim, MIT). Ver src/assets/now-ui/LICENSE.md
import 'bootstrap/dist/css/bootstrap.min.css'
import './assets/now-ui/css/now-ui-dashboard.min.css'
import './app.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
