
# Ineffective Verbal CommunicationAnalysis

This Java software implements a detector of ineffective verbal communication in training session conducted within simulated environments in healthcare. The system analyses super-segmental features extracted from a speech signal after a segmentation of the signal into tone units. 

It uses the following features: **Energy, Pitch, and Modulation Spectrum**. Delta and double deltas are also extracted for all features.

The software parametrisation is given in the **config.properties** file.

Internally, the software uses two Variational Autoencoders in cascade: the first VAE detects non-speech segments, which are later excluded by the processing pipeline. The second VAE detects speech segments that include ineffective communication characterised by 

 - High emotional and psychological stress
 - Communication not addressing the appropriate team member
 - Non-assertive or aggressive communication style
 - Uncontrolled emotional content and verbal intonation
 - Uncertainty in the communication
 - Confusion between technical terms
 - Confirmation of action asked to the other team members
 - Repeated use of first person plural
 - Low leadership by the doctor leading the operation
 - Discussion about strategies during the emergency
 - Low level of expertise in the specific task

The software does not use automatic speech recognition, because it is conceived for managing high-noise conditions with multiple speakers talking.

The [Main](https://github.com/cybprojects65/IneffectiveVerbalCommunicationAnalysis/blob/main/src/it/cnr/aoup/ninia/anomalydetection/main/Main.java) class is the entry point of the software. For using it, import the code as a Java project in Eclipse, set the input file, and execute the Main class.

The [Corpus_16kHz](https://github.com/cybprojects65/IneffectiveVerbalCommunicationAnalysis/tree/main/Corpus_16kHz) folder contains the corpus of neonatal training through simulation used to evaluate the software with all automatic and reference annotations.

The [vae_models](https://github.com/cybprojects65/IneffectiveVerbalCommunicationAnalysis/tree/main/vae_models) folder contains all VAE models trained and projected on the corpus audio files.

## Required projects
[Modulation Spectrogram](https://github.com/cybprojects65/ModulationSpectrogram)
[VariationalAutoencoder](https://github.com/cybprojects65/VariationalAutoencoder)
