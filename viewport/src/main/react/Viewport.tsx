import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import { BrowserRouter } from 'react-router-dom'
import { SkeletonTheme } from 'react-loading-skeleton'
import { PageRouter } from './components/routing/PageRouter'
import { Cursor } from './components/cursor/Cursor'
import { ViewportServices } from './lib/services/ViewportServices'

ViewportServices.get(); // Call once to initialize

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
        <SkeletonTheme baseColor='#262626' highlightColor='#737373'>
            <Cursor />
            <PageRouter />
        </SkeletonTheme>
    </BrowserRouter>
  </React.StrictMode>,
)