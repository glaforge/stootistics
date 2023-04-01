import { createApp } from 'vue'
import './style.css'
import AppMastodonReachForm from './components/MastodonReachForm.vue'

import '@shoelace-style/shoelace/dist/themes/light.css';
import { setBasePath } from '@shoelace-style/shoelace/dist/utilities/base-path';
setBasePath('https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.3.0/dist/');

createApp(AppMastodonReachForm).mount('#app')

