import React from 'react';
import Modal from 'react-modal';

// Set the app element for accessibility
Modal.setAppElement('#root');

const modalStyles = {
  content: {
    top: '50%',
    left: '50%',
    right: 'auto',
    bottom: 'auto',
    marginRight: '-50%',
    transform: 'translate(-50%, -50%)',
    maxWidth: '400px',
    width: '90%',
    padding: '24px 20px 20px 20px',
    borderRadius: '12px',
    boxShadow: '0 4px 16px rgba(34,86,34,0.13)',
    background: '#fffaf6', // light peach
    border: '2px solid #ffe5b4', // light peach border
    color: '#225622', // dark green text
    fontFamily: "'Segoe UI', 'Roboto', Arial, sans-serif"
  },
  overlay: {
    backgroundColor: 'rgba(34, 86, 34, 0.10)'
  }
};

const ReceivedItemModal = ({ isOpen, onRequestClose, item }) => {
  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={onRequestClose}
      style={modalStyles}
      contentLabel="Received Clothing Item"
    >
      <h2 style={{ color: '#225622', fontWeight: 700, marginBottom: 8 }}>You received:</h2>
      <h3 style={{ color: '#225622', fontWeight: 600, marginBottom: 12 }}>{item?.name}</h3>
      {item?.imageUrl && (
        <img
          src={item.imageUrl}
          alt={item.name}
          style={{
            maxWidth: '100%',
            height: 'auto',
            marginTop: '10px',
            borderRadius: '8px',
            background: '#ffe5b4',
            boxShadow: '0 2px 8px #ffe5b4'
          }}
        />
      )}
      <button
        onClick={onRequestClose}
        style={{
          marginTop: '24px',
          background: '#225622',
          color: '#fff',
          border: 'none',
          borderRadius: '8px',
          padding: '10px 28px',
          fontWeight: 600,
          fontSize: '1.1em',
          cursor: 'pointer',
          boxShadow: '0 2px 8px #ffe5b4',
          transition: 'background 0.15s'
        }}
      >
        Close
      </button>
    </Modal>
  );
};

export default ReceivedItemModal;