// ============================================================================
// AUCTION SYSTEM - JAVASCRIPT PRINCIPAL (VERSION WEBSOCKET JADE)
// ============================================================================

// État global de l'application
const appState = {
    auctions: [],
    agents: [],
    logs: [],
    stats: {
        activeAuctions: 0,
        activeAgents: 0,
        totalVolume: 0,
        totalBids: 0,
        completedAuctions: 0
    },
    isPaused: false,
    isConnected: false
};

// ============================================================================
// INITIALISATION
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Application démarrée');
    
    initTabs();
    initFilters();
    initModal();
    initLogControls();
    initCharts();
    
    // Connexion WebSocket au serveur JADE
    if (typeof initWebSocket === 'function') {
        console.log('🔌 Initialisation de la connexion WebSocket...');
        initWebSocket();
    } else {
        console.warn('⚠️ Module WebSocket non chargé, utilisation du mode simulation');
        addLog('⚠️ Mode simulation (WebSocket non disponible)', 'warning');
        // Décommentez la ligne suivante pour activer la simulation si pas de WebSocket
        // startSimulation();
    }
    
    // Mise à jour périodique de l'interface
    setInterval(updateUI, 1000);
});

// ============================================================================
// GESTION DES ONGLETS
// ============================================================================

function initTabs() {
    const tabs = document.querySelectorAll('.tab');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const targetTab = tab.dataset.tab;
            
            // Retirer active de tous
            tabs.forEach(t => t.classList.remove('active'));
            tabContents.forEach(tc => tc.classList.remove('active'));
            
            // Activer le bon
            tab.classList.add('active');
            document.getElementById(`${targetTab}-tab`).classList.add('active');
            
            console.log(`📑 Onglet activé: ${targetTab}`);
        });
    });
}

// ============================================================================
// GESTION DES FILTRES
// ============================================================================

function initFilters() {
    const filterBtns = document.querySelectorAll('.filter-btn');
    
    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            const filter = btn.dataset.filter;
            filterAgents(filter);
        });
    });
}

function filterAgents(filter) {
    const agentRows = document.querySelectorAll('.agent-row');
    
    agentRows.forEach(row => {
        if (filter === 'all' || row.dataset.type === filter) {
            row.style.display = 'flex';
        } else {
            row.style.display = 'none';
        }
    });
}

// ============================================================================
// AFFICHAGE DES ENCHÈRES
// ============================================================================

function renderAuctions() {
    const container = document.getElementById('auctions-grid');
    
    if (!container) return;
    
    if (appState.auctions.length === 0) {
        container.innerHTML = `
            <div class="no-data">
                <p>📭 Aucune enchère active</p>
                <small>En attente de données du serveur JADE...</small>
            </div>
        `;
        return;
    }
    
    container.innerHTML = appState.auctions.map(auction => `
        <div class="auction-card" data-id="${auction.id}">
            <div class="auction-header">
                <h3 class="auction-title">${auction.name}</h3>
                <span class="status-badge live">🔴 LIVE</span>
            </div>
            
            <div class="auction-price">
                <div class="price-label">Prix Actuel</div>
                <div class="price-value">${auction.currentPrice.toFixed(2)}€</div>
            </div>
            
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${Math.min((auction.currentPrice / auction.maxPrice) * 100, 100)}%"></div>
            </div>
            <small style="color: #7f8c8d;">${Math.round(Math.min((auction.currentPrice / auction.maxPrice) * 100, 100))}% du prix max</small>
            
            <div class="auction-info">
                <span class="leader-icon">👑</span>
                <div>
                    <div style="font-size: 0.85rem; color: #7f8c8d;">Leader Actuel</div>
                    <div style="font-weight: bold;">${auction.winner || 'Aucun'}</div>
                </div>
            </div>
            
            <div class="time-left">
                ⏰ Temps restant: ${formatTime(auction.timeLeft)}
            </div>
            
            <button class="btn-details" onclick="showAuctionDetails('${auction.id}')">
                Voir les détails →
            </button>
        </div>
    `).join('');
}

// ============================================================================
// AFFICHAGE DES AGENTS
// ============================================================================

function renderAgents() {
    const container = document.getElementById('agents-list');
    
    if (!container) {
        console.error('Container agents-list introuvable !');
        return;
    }
    
    if (appState.agents.length === 0) {
        container.innerHTML = '<p style="text-align:center;color:#999;padding:40px;">Aucun agent actif</p>';
        return;
    }
    
    console.log('Rendu de', appState.agents.length, 'agents');
    
    container.innerHTML = appState.agents.map(agent => `
        <div class="agent-row" data-type="${agent.type}">
            <div class="agent-avatar ${agent.type}">
                ${getAgentIcon(agent.type)}
            </div>
            <div class="agent-info">
                <div class="agent-name">${agent.name}</div>
                <div class="agent-activity">${agent.bids} offres • ${agent.activity}</div>
            </div>
            <div class="agent-budget">
                <div class="budget-label">Budget</div>
                <div class="budget-value">${agent.budget.toFixed(2)}€</div>
            </div>
            <div class="agent-status"></div>
        </div>
    `).join('');
}

function getAgentIcon(type) {
    const icons = {
        aggressive: '🔥',
        conservative: '🐢',
        intelligent: '🧠',
        auctioneer: '🎯',
        monitor: '📊',
        bank: '🏦',
        other: '🤖'
    };
    return icons[type] || icons.other;
}

// ============================================================================
// AFFICHAGE DES LOGS
// ============================================================================

function addLog(message, type = 'info') {
    if (appState.isPaused) return;
    
    const log = {
        time: new Date(),
        message,
        type
    };
    
    appState.logs.unshift(log);
    
    // Limiter à 100 logs
    if (appState.logs.length > 100) {
        appState.logs = appState.logs.slice(0, 100);
    }
    
    renderLogs();
}

function renderLogs() {
    const container = document.getElementById('logs-container');
    
    if (!container) return;
    
    if (appState.logs.length === 0) {
        container.innerHTML = '<div class="log-entry info"><span>📝 En attente d\'événements...</span></div>';
        return;
    }
    
    container.innerHTML = appState.logs.map(log => `
        <div class="log-entry ${log.type}">
            <span class="log-time">${formatLogTime(log.time)}</span>
            <span>${log.message}</span>
        </div>
    `).join('');
}

function initLogControls() {
    const clearBtn = document.getElementById('clearLogs');
    const pauseBtn = document.getElementById('pauseLogs');
    
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            appState.logs = [];
            renderLogs();
            addLog('📝 Journal effacé', 'info');
        });
    }
    
    if (pauseBtn) {
        pauseBtn.addEventListener('click', (e) => {
            appState.isPaused = !appState.isPaused;
            e.target.textContent = appState.isPaused ? '▶️ Reprendre' : '⏸️ Pause';
            addLog(appState.isPaused ? '⏸️ Journal en pause' : '▶️ Journal repris', 'info');
        });
    }
}

// ============================================================================
// MODAL
// ============================================================================

function initModal() {
    const modal = document.getElementById('auctionModal');
    const closeBtn = modal?.querySelector('.close');
    
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            modal.style.display = 'none';
        });
    }
    
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
}

function showAuctionDetails(auctionId) {
    const auction = appState.auctions.find(a => a.id === auctionId);
    if (!auction) return;
    
    const modal = document.getElementById('auctionModal');
    const modalBody = document.getElementById('modalBody');
    
    if (!modal || !modalBody) return;
    
    modalBody.innerHTML = `
        <h2>${auction.name}</h2>
        <div style="margin: 20px 0;">
            <h3>📋 Informations</h3>
            <p><strong>ID:</strong> ${auction.id}</p>
            <p><strong>Prix actuel:</strong> ${auction.currentPrice.toFixed(2)}€</p>
            <p><strong>Prix de départ:</strong> ${auction.startPrice?.toFixed(2) || 'N/A'}€</p>
            <p><strong>Prix maximum:</strong> ${auction.maxPrice.toFixed(2)}€</p>
            <p><strong>Gagnant actuel:</strong> ${auction.winner || 'Aucun'}</p>
            <p><strong>Temps restant:</strong> ${formatTime(auction.timeLeft)}</p>
            <p><strong>Nombre d'offres:</strong> ${auction.bidCount || 0}</p>
        </div>
        <div style="margin: 20px 0;">
            <h3>📊 Historique des offres</h3>
            <div id="bidHistory">
                ${auction.bidHistory ? auction.bidHistory.map(bid => `
                    <div style="padding: 10px; background: #f5f7fa; margin: 5px 0; border-radius: 8px;">
                        <strong>${bid.agent}</strong> - ${bid.amount.toFixed(2)}€ 
                        <small style="color: #7f8c8d;">(${new Date(bid.time).toLocaleTimeString()})</small>
                    </div>
                `).join('') : '<p>Aucune offre pour le moment</p>'}
            </div>
        </div>
    `;
    
    modal.style.display = 'block';
}

// ============================================================================
// GRAPHIQUES
// ============================================================================

let priceChart, agentChart;

function initCharts() {
    // Graphique des prix
    const priceCtx = document.getElementById('priceChart');
    if (priceCtx && typeof Chart !== 'undefined') {
        priceChart = new Chart(priceCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Prix moyen',
                    data: [],
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.1)',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
    
    // Graphique des agents
    const agentCtx = document.getElementById('agentChart');
    if (agentCtx && typeof Chart !== 'undefined') {
        agentChart = new Chart(agentCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Nombre d\'offres',
                    data: [],
                    backgroundColor: [
                        '#e74c3c',
                        '#3498db',
                        '#9b59b6',
                        '#f39c12',
                        '#27ae60'
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
}

function updateCharts() {
    // Mettre à jour le graphique des prix
    if (priceChart && appState.auctions.length > 0) {
        const avgPrice = appState.auctions.reduce((sum, a) => sum + a.currentPrice, 0) / appState.auctions.length;
        const time = new Date().toLocaleTimeString();
        
        priceChart.data.labels.push(time);
        priceChart.data.datasets[0].data.push(avgPrice);
        
        // Garder seulement les 10 derniers points
        if (priceChart.data.labels.length > 10) {
            priceChart.data.labels.shift();
            priceChart.data.datasets[0].data.shift();
        }
        
        priceChart.update('none'); // Update sans animation pour performance
    }
    
    // Mettre à jour le graphique des agents
    if (agentChart && appState.agents.length > 0) {
        const topAgents = [...appState.agents]
            .sort((a, b) => (b.bids || 0) - (a.bids || 0))
            .slice(0, 5);
        
        agentChart.data.labels = topAgents.map(a => a.name);
        agentChart.data.datasets[0].data = topAgents.map(a => a.bids || 0);
        
        agentChart.update('none');
    }
}

// ============================================================================
// MISE À JOUR UI
// ============================================================================

function updateUI() {
    // Mettre à jour les statistiques du header
    document.getElementById('activeAuctions').textContent = appState.stats.activeAuctions;
    document.getElementById('activeAgents').textContent = appState.stats.activeAgents;
    document.getElementById('totalVolume').textContent = appState.stats.totalVolume.toFixed(0) + '€';
    
    // Mettre à jour les statistiques analytics
    const totalBidsEl = document.querySelector('#analytics-tab .stat-item:nth-child(1) .stat-number');
    const avgPriceEl = document.querySelector('#analytics-tab .stat-item:nth-child(2) .stat-number');
    const completedEl = document.querySelector('#analytics-tab .stat-item:nth-child(3) .stat-number');
    const successRateEl = document.querySelector('#analytics-tab .stat-item:nth-child(4) .stat-number');
    
    if (totalBidsEl) totalBidsEl.textContent = appState.stats.totalBids;
    if (avgPriceEl) {
        const avg = appState.stats.activeAuctions > 0 
            ? appState.stats.totalVolume / appState.stats.activeAuctions 
            : 0;
        avgPriceEl.textContent = avg.toFixed(0) + '€';
    }
    if (completedEl) completedEl.textContent = appState.stats.completedAuctions;
    if (successRateEl) {
        const rate = appState.stats.completedAuctions > 0 
            ? Math.round((appState.stats.completedAuctions / (appState.stats.completedAuctions + appState.stats.activeAuctions)) * 100)
            : 0;
        successRateEl.textContent = rate + '%';
    }
    
    // Décrémenter le temps restant des enchères
    appState.auctions.forEach(auction => {
        if (auction.timeLeft > 0) {
            auction.timeLeft -= 1;
        }
    });
    
    // Mise à jour périodique des graphiques (toutes les 5 secondes)
    if (Date.now() % 5000 < 1000) {
        updateCharts();
    }
}

// ============================================================================
// UTILITAIRES
// ============================================================================

function formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
}

function formatLogTime(date) {
    return date.toLocaleTimeString('fr-FR');
}

// ============================================================================
// API POUR WEBSOCKET (Appelée depuis websocket.js)
// ============================================================================

window.auctionAPI = {
    // Ajouter une nouvelle enchère
    addAuction: (auction) => {
        console.log('➕ Ajout enchère:', auction);
        const existingIndex = appState.auctions.findIndex(a => a.id === auction.id);
        if (existingIndex === -1) {
            appState.auctions.push(auction);
            appState.stats.activeAuctions = appState.auctions.length;
            appState.stats.totalVolume = appState.auctions.reduce((sum, a) => sum + a.currentPrice, 0);
            renderAuctions();
            addLog(`🆕 Nouvelle enchère: ${auction.name}`, 'info');
        }
    },
    
    // Mettre à jour une enchère
    updateAuction: (auctionId, data) => {
        console.log('🔄 Mise à jour enchère:', auctionId, data);
        const auction = appState.auctions.find(a => a.id === auctionId);
        if (auction) {
            Object.assign(auction, data);
            appState.stats.totalVolume = appState.auctions.reduce((sum, a) => sum + a.currentPrice, 0);
            renderAuctions();
            
            if (data.currentPrice) {
                addLog(`💰 ${data.winner} a offert ${data.currentPrice.toFixed(2)}€ sur ${auction.name}`, 'info');
                highlightAuction(auctionId);
            }
        }
    },
    
    // Terminer une enchère
    endAuction: (auctionId, winner, finalPrice) => {
        console.log('🏁 Fin enchère:', auctionId);
        const index = appState.auctions.findIndex(a => a.id === auctionId);
        if (index !== -1) {
            const auction = appState.auctions[index];
            appState.auctions.splice(index, 1);
            appState.stats.activeAuctions = appState.auctions.length;
            appState.stats.completedAuctions++;
            appState.stats.totalVolume = appState.auctions.reduce((sum, a) => sum + a.currentPrice, 0);
            renderAuctions();
            addLog(`🏁 Enchère terminée: ${auction.name} - Gagnant: ${winner} (${finalPrice.toFixed(2)}€)`, 'success');
        }
    },
    
    // Ajouter un agent
    addAgent: (agent) => {
        console.log('➕ Ajout agent:', agent);
        const existingIndex = appState.agents.findIndex(a => a.name === agent.name);
        if (existingIndex === -1) {
            appState.agents.push(agent);
            appState.stats.activeAgents = appState.agents.length;
            renderAgents();
            addLog(`👤 Agent connecté: ${agent.name}`, 'info');
        }
    },
    
    // Mettre à jour un agent
    updateAgent: (agentName, data) => {
        console.log('🔄 Mise à jour agent:', agentName, data);
        const agent = appState.agents.find(a => a.name === agentName);
        if (agent) {
            Object.assign(agent, data);
            if (data.bids !== undefined) {
                appState.stats.totalBids = appState.agents.reduce((sum, a) => sum + (a.bids || 0), 0);
            }
            renderAgents();
        }
    },
    
    // Ajouter un log
    log: (message, type = 'info') => {
        addLog(message, type);
    },
    
    // Mettre à jour les stats
    updateStats: (stats) => {
        console.log('📊 Mise à jour stats:', stats);
        Object.assign(appState.stats, stats);
    }
};

// ============================================================================
// EFFETS VISUELS
// ============================================================================

function highlightAuction(auctionId) {
    const card = document.querySelector(`[data-id="${auctionId}"]`);
    if (card) {
        card.style.transform = 'scale(1.05)';
        card.style.boxShadow = '0 8px 30px rgba(102, 126, 234, 0.4)';
        card.style.transition = 'all 0.3s ease';
        
        setTimeout(() => {
            card.style.transform = '';
            card.style.boxShadow = '';
        }, 500);
    }
}

// ============================================================================
// EXPORT GLOBAL
// ============================================================================

console.log('✅ main.js chargé - API WebSocket prête');