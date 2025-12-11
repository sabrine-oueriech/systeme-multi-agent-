// ============================================================================
// WEBSOCKET CLIENT - Connexion Temps Réel avec JADE
// ============================================================================

let ws = null;
let reconnectInterval = null;
let isConnected = false;

// ============================================================================
// INITIALISATION WEBSOCKET
// ============================================================================

function initWebSocket() {
    const wsUrl = 'ws://localhost:9090';
    console.log('🔌 Connexion à WebSocket:', wsUrl);
    
    try {
        ws = new WebSocket(wsUrl);
        
        ws.onopen = onWebSocketOpen;
        ws.onmessage = onWebSocketMessage;
        ws.onerror = onWebSocketError;
        ws.onclose = onWebSocketClose;
        
    } catch (error) {
        console.error('❌ Erreur WebSocket:', error);
        scheduleReconnect();
    }
}

// ============================================================================
// GESTIONNAIRES D'ÉVÉNEMENTS WEBSOCKET
// ============================================================================

function onWebSocketOpen(event) {
    console.log('✅ WebSocket connecté');
    isConnected = true;
    
    // Mettre à jour le statut de connexion
    updateConnectionStatus(true);
    
    // Envoyer un message de test
    sendMessage({
        type: 'CLIENT_READY',
        timestamp: Date.now()
    });
    
    // Arrêter les tentatives de reconnexion
    if (reconnectInterval) {
        clearInterval(reconnectInterval);
        reconnectInterval = null;
    }
}

function onWebSocketMessage(event) {
    try {
        const data = JSON.parse(event.data);
        console.log('📨 Message reçu:', data);
        
        // Router le message selon son type
        handleWebSocketMessage(data);
        
    } catch (error) {
        console.error('❌ Erreur traitement message:', error);
    }
}

function onWebSocketError(error) {
    console.error('❌ Erreur WebSocket:', error);
    updateConnectionStatus(false);
}

function onWebSocketClose(event) {
    console.log('❌ WebSocket déconnecté');
    isConnected = false;
    updateConnectionStatus(false);
    
    // Tenter une reconnexion
    scheduleReconnect();
}

// ============================================================================
// TRAITEMENT DES MESSAGES
// ============================================================================

function handleWebSocketMessage(data) {
    switch (data.type) {
        case 'INITIAL_STATE':
            handleInitialState(data.data);
            break;
            
        case 'NEW_AUCTION':
            handleNewAuction(data.data);
            break;
            
        case 'BID_UPDATE':
            handleBidUpdate(data.data);
            break;
            
        case 'AUCTION_END':
            handleAuctionEnd(data.data);
            break;
            
        case 'AGENT_UPDATE':
            handleAgentUpdate(data.data);
            break;
            
        case 'LOG':
            handleLog(data.data);
            break;
            
        case 'STATS_UPDATE':
            handleStatsUpdate(data.data);
            break;
            
        default:
            console.warn('Type de message inconnu:', data.type);
    }
}

// ============================================================================
// HANDLERS SPÉCIFIQUES
// ============================================================================

function handleInitialState(data) {
    console.log('📦 État initial reçu:', data);
    addLog('Connecté au système JADE', 'success');
}

function handleNewAuction(auction) {
    console.log('🆕 Nouvelle enchère:', auction);
    
    // Ajouter à l'état global
    const existingIndex = appState.auctions.findIndex(a => a.id === auction.id);
    if (existingIndex === -1) {
        appState.auctions.push(auction);
        addLog(`Nouvelle enchère créée: ${auction.name}`, 'info');
    }
    
    // Mettre à jour l'affichage
    renderAuctions();
}

function handleBidUpdate(update) {
    console.log('💰 Mise à jour enchère:', update);
    
    // Trouver l'enchère et la mettre à jour
    const auction = appState.auctions.find(a => a.id === update.id);
    if (auction) {
        auction.currentPrice = update.currentPrice;
        auction.winner = update.winner;
        
        addLog(`${update.winner} a offert ${update.currentPrice.toFixed(2)}€ sur ${update.id}`, 'info');
        
        // Animation de mise à jour
        highlightAuction(update.id);
    }
    
    // Mettre à jour l'affichage
    renderAuctions();
}

function handleAuctionEnd(data) {
    console.log('🏁 Enchère terminée:', data);
    
    // Retirer l'enchère de la liste
    const index = appState.auctions.findIndex(a => a.id === data.id);
    if (index !== -1) {
        appState.auctions.splice(index, 1);
        addLog(`🎉 Enchère ${data.id} terminée - Gagnant: ${data.winner} (${data.finalPrice.toFixed(2)}€)`, 'success');
    }
    
    // Mettre à jour l'affichage
    renderAuctions();
}

function handleAgentUpdate(agent) {
    console.log('👤 Mise à jour agent:', agent);
    
    // Trouver l'agent et le mettre à jour
    const existingIndex = appState.agents.findIndex(a => a.name === agent.name);
    if (existingIndex === -1) {
        appState.agents.push(agent);
    } else {
        appState.agents[existingIndex] = agent;
    }
    
    // Mettre à jour l'affichage
    renderAgents();
}

function handleLog(log) {
    addLog(log.message, log.level);
}

function handleStatsUpdate(stats) {
    console.log('📊 Statistiques:', stats);
    
    // Mettre à jour les statistiques globales
    appState.stats.activeAuctions = stats.activeAuctions;
    appState.stats.activeAgents = stats.activeAgents;
    appState.stats.totalVolume = stats.totalVolume;
    
    // Mettre à jour l'affichage
    updateUI();
}

// ============================================================================
// EFFETS VISUELS
// ============================================================================

function highlightAuction(auctionId) {
    const card = document.querySelector(`[data-id="${auctionId}"]`);
    if (card) {
        card.style.transform = 'scale(1.05)';
        card.style.boxShadow = '0 8px 30px rgba(102, 126, 234, 0.4)';
        
        setTimeout(() => {
            card.style.transform = '';
            card.style.boxShadow = '';
        }, 500);
    }
}

// ============================================================================
// STATUT DE CONNEXION
// ============================================================================

function updateConnectionStatus(connected) {
    const statusDot = document.getElementById('connectionStatus');
    const statusText = document.getElementById('connectionText');
    
    if (statusDot && statusText) {
        if (connected) {
            statusDot.classList.remove('disconnected');
            statusText.textContent = 'Connecté à JADE';
            statusText.style.color = '#27ae60';
        } else {
            statusDot.classList.add('disconnected');
            statusText.textContent = 'Déconnecté';
            statusText.style.color = '#e74c3c';
        }
    }
}

// ============================================================================
// RECONNEXION
// ============================================================================

function scheduleReconnect() {
    if (reconnectInterval) return;
    
    console.log('🔄 Tentative de reconnexion dans 5 secondes...');
    
    reconnectInterval = setInterval(() => {
        console.log('🔄 Reconnexion...');
        initWebSocket();
    }, 5000);
}

// ============================================================================
// ENVOI DE MESSAGES
// ============================================================================

function sendMessage(data) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(data));
    } else {
        console.warn('⚠️ WebSocket non connecté');
    }
}

// ============================================================================
// NETTOYAGE
// ============================================================================

window.addEventListener('beforeunload', () => {
    if (ws) {
        ws.close();
    }
});

// ============================================================================
// EXPORT GLOBAL
// ============================================================================

window.WebSocketClient = {
    init: initWebSocket,
    send: sendMessage,
    isConnected: () => isConnected
};

// Auto-initialisation
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initWebSocket);
} else {
    initWebSocket();
}