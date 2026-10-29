/* =========================================================
   Pratiksha AI - Maya Live Persona Logic
   Features: Interactive lip-sync, gestures, speech/chat simulation
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {
  // DOM Elements
  const chatFeed = document.getElementById('chatFeed');
  const userInput = document.getElementById('userInput');
  const sendBtn = document.getElementById('sendBtn');
  const micBtn = document.getElementById('micBtn');
  const avatarCard = document.getElementById('avatarCard');
  const avatarWrapper = document.getElementById('avatarWrapper');
  const mayaAvatar = document.getElementById('mayaAvatar');
  const speechStatus = document.getElementById('speechStatus');
  const speechStatusFooter = document.getElementById('speechStatusFooter');
  const moodBadge = document.getElementById('moodBadge');
  const voiceToggle = document.getElementById('voiceToggle');
  const promptChips = document.getElementById('promptChips');

  let isVoiceEnabled = true;
  let isListening = false;
  let recognition = null;
  let synth = window.speechSynthesis || null;

  // Maya Persona Responses Knowledge Base
  const mayaKnowledge = [
    {
      keywords: ['hero', 'design', 'website', 'card', 'ui'],
      mood: '✨ Designing',
      response: "Hero section banana bohot simple aur sundar ho sakta hai! Main ek modern mobile glassmorphism design code ready karti hoon."
    },
    {
      keywords: ['code', 'js', 'demo', 'interactive', 'cute'],
      mood: '💻 Coding',
      response: "Yeh raha aapke liye ek cute micro interaction function: \n\n```js\nfunction cheerUser() {\n  console.log('Maya loves building UI! 🚀');\n}\n```"
    },
    {
      keywords: ['capability', 'kya kar', 'help', 'who are you', 'maya'],
      mood: '💖 Helpful',
      response: "Main Maya hoon! Pratiksha AI ki live coding assistant. Main har interface mobile-first, soft blue & white themes, responsive layout aur clean code se banati hoon!"
    },
    {
      keywords: ['hi', 'hello', 'namaste', 'hey'],
      mood: '😊 Friendly',
      response: "Namaste! Kaise ho aap? Aaj hum kya naya create kar rahe hain?"
    }
  ];

  const defaultResponses = [
    "Aapne bohot badhiya baat boli! Main is par soch kar stylish design code taiyar kar sakti hoon. ✨",
    "Samajh gayi! Mobile screen par yeh kitna cute aur clean dikhega, chaliye milkar banate hain! 💙",
    "Sahi pakde hain! Main poori tarah aapki madad ke liye taiyar hoon. Aur kya details chahiye?"
  ];

  // Speech Recognition Setup
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    recognition = new SpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = false;
    recognition.lang = 'hi-IN';

    recognition.onstart = () => {
      isListening = true;
      micBtn.classList.add('listening');
      updateStatus("Listening... Bolne ki koshish karein");
      triggerLipSync(false);
    };

    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript;
      userInput.value = transcript;
      updateStatus("Aapne bola: " + transcript);
      handleUserSendMessage();
    };

    recognition.onerror = () => {
      updateStatus("Audio sunne me dikkat hui, type kar sakte hain.");
      micBtn.classList.remove('listening');
      isListening = false;
    };

    recognition.onend = () => {
      micBtn.classList.remove('listening');
      isListening = false;
    };
  }

  function updateStatus(text) {
    if (speechStatus) speechStatus.textContent = text;
    if (speechStatusFooter) speechStatusFooter.textContent = text;
  }

  // Handle Voice Toggle
  voiceToggle.addEventListener('click', () => {
    isVoiceEnabled = !isVoiceEnabled;
    voiceToggle.classList.toggle('active', isVoiceEnabled);
    updateStatus(isVoiceEnabled ? "Voice Output Enabled 🔊" : "Voice Output Muted 🔇");
  });

  // Tap Avatar for Gesture & Wink
  avatarWrapper.addEventListener('click', () => {
    moodBadge.textContent = "😉 Cute Wink";
    avatarCard.classList.add('thinking');
    
    // Animate Wink
    const eyeRight = document.getElementById('eyeRight');
    if (eyeRight) {
      eyeRight.setAttribute('ry', '1');
    }

    setTimeout(() => {
      if (eyeRight) eyeRight.setAttribute('ry', '9');
      avatarCard.classList.remove('thinking');
      moodBadge.textContent = "✨ Friendly";
    }, 1000);
  });

  // Handle Prompts Chips
  promptChips.addEventListener('click', (e) => {
    const chip = e.target.closest('.chip');
    if (chip) {
      userInput.value = chip.getAttribute('data-prompt');
      handleUserSendMessage();
    }
  });

  // Send Message Trigger
  sendBtn.addEventListener('click', handleUserSendMessage);
  userInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') handleUserSendMessage();
  });

  // Mic Button Trigger
  micBtn.addEventListener('click', () => {
    if (!recognition) {
      alert("Browser voice recognition support nahi kar raha. Direct type karein!");
      return;
    }
    if (isListening) {
      recognition.stop();
    } else {
      try {
        recognition.start();
      } catch (err) {
        console.log(err);
      }
    }
  });

  function handleUserSendMessage() {
    const text = userInput.value.trim();
    if (!text) return;

    // Render User Message
    renderMessage('user', text);
    userInput.value = '';

    // Show Maya Thinking State
    updateStatus("Maya is thinking...");
    avatarCard.classList.add('thinking');
    moodBadge.textContent = "🤔 Thinking...";

    setTimeout(() => {
      avatarCard.classList.remove('thinking');
      generateMayaResponse(text);
    }, 1000);
  }

  function generateMayaResponse(userText) {
    const textLower = userText.toLowerCase();
    let matched = mayaKnowledge.find(k => k.keywords.some(kw => textLower.includes(kw)));

    let responseText = matched ? matched.response : defaultResponses[Math.floor(Math.random() * defaultResponses.length)];
    let responseMood = matched ? matched.mood : "💖 Friendly";

    moodBadge.textContent = responseMood;
    updateStatus("Maya is talking...");

    // Start Lip Syncing Animations
    triggerLipSync(true);

    // Render Maya Message
    renderMessage('maya', responseText);

    // Speak using Web Speech Synthesis if enabled
    if (isVoiceEnabled && synth) {
      speakText(responseText, () => {
        triggerLipSync(false);
        updateStatus("Maya is listening & ready...");
        moodBadge.textContent = "✨ Friendly";
      });
    } else {
      // Fallback timer for animation lip sync
      setTimeout(() => {
        triggerLipSync(false);
        updateStatus("Maya is listening & ready...");
        moodBadge.textContent = "✨ Friendly";
      }, Math.min(responseText.length * 50, 4000));
    }
  }

  function triggerLipSync(active) {
    if (active) {
      avatarCard.classList.add('speaking');
      avatarWrapper.classList.add('lip-talking');
    } else {
      avatarCard.classList.remove('speaking');
      avatarWrapper.classList.remove('lip-talking');
    }
  }

  function speakText(text, onEndCallback) {
    if (!synth) {
      onEndCallback();
      return;
    }

    synth.cancel(); // Stop any ongoing speech
    // Clean text of code blocks or markdown emojis for speech
    const cleanText = text.replace(/```[\s\S]*?```/g, "Code element provided.").replace(/[^a-zA-Z0-9\s,.?!]/g, "");

    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.rate = 1.0;
    utterance.pitch = 1.1; // Slightly sweet/cute pitch

    utterance.onend = () => {
      onEndCallback();
    };

    utterance.onerror = () => {
      onEndCallback();
    };

    synth.speak(utterance);
  }

  function renderMessage(sender, text) {
    const bubble = document.createElement('div');
    bubble.className = `message ${sender}`;

    const content = document.createElement('div');
    content.className = 'msg-content';

    // Handle Code Markdown Formatting cleanly without innerHTML risk
    if (text.includes('```')) {
      const parts = text.split(/```/);
      parts.forEach((part, idx) => {
        if (idx % 2 === 1) {
          const pre = document.createElement('pre');
          pre.textContent = part.replace(/^js|^css|^html/, '').trim();
          content.appendChild(pre);
        } else if (part.trim()) {
          const p = document.createElement('span');
          p.textContent = part;
          content.appendChild(p);
        }
      });
    } else {
      content.textContent = text;
    }

    bubble.appendChild(content);
    chatFeed.appendChild(bubble);
    chatFeed.scrollTop = chatFeed.scrollHeight;
  }
});
