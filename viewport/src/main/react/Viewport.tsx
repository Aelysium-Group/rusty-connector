import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import { BrowserRouter } from 'react-router-dom'
import { SkeletonTheme } from 'react-loading-skeleton'
import { PageRouter } from './components/routing/PageRouter'
import { Cursor } from './components/cursor/Cursor'
import { ViewportServices } from './lib/services/ViewportServices'
import { Dynav } from './components/dynav/Dynav'

ViewportServices.get(); // Call once to initialize

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
        <SkeletonTheme baseColor='#262626' highlightColor='#737373'>
            <Dynav />
            <PageRouter />
            <Cursor />
        </SkeletonTheme>
    </BrowserRouter>
  </React.StrictMode>,
)