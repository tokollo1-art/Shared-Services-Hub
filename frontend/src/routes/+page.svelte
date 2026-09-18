<script>
	import { isAuthenticated, currentUser, getRoleDashboard } from '$lib/stores/auth.js';

	const loggedIn = $derived($isAuthenticated);
	const user = $derived($currentUser);
	const dashboardUrl = $derived(user?.role ? getRoleDashboard(user.role) : '/dashboard/youth');

	const steps = [
		{
			step: '01',
			title: 'Corporate Funds Cohort',
			description: 'Corporates allocate enterprise development and B-BBEE funds directly into youth wage reserves.',
			tag: 'Corporate'
		},
		{
			step: '02',
			title: 'Youth Enters Hub',
			description: 'Trained youth program graduates enter SSH ready for real-world SME tasks.',
			tag: 'Youth'
		},
		{
			step: '03',
			title: 'SME Posts Free Task',
			description: 'Vetted small businesses post high-priority tasks with zero commission or hiring fees.',
			tag: 'SME'
		},
		{
			step: '04',
			title: 'Youth Delivers Work',
			description: 'Matched candidates execute the task with guided milestones and quality standards.',
			tag: 'Youth'
		},
		{
			step: '05',
			title: 'SME Verifies Quality',
			description: 'The business rates the work and provides honest feedback on task completion.',
			tag: 'SME'
		},
		{
			step: '06',
			title: 'SSH Pays Directly',
			description: 'Wages disburse straight to the youth from the funded reserve. SMEs never handle cash.',
			tag: 'Fintech'
		},
		{
			step: '07',
			title: 'Ledger Records Proof',
			description: 'Verified work enters the Experience Ledger — portable, trusted proof of capability.',
			tag: 'Ledger'
		},
		{
			step: '08',
			title: 'Permanent Placement',
			description: 'Youth leverage their verified track record to transition into full-time employment.',
			tag: 'Alumni'
		}
	];
</script>

<svelte:head>
	<title>Shared Services Hub | Work. Income. Experience. Guaranteed.</title>
</svelte:head>

<!-- Hero Section (Sharp, Cape Cobalt Editorial) -->
<section class="relative bg-[#F8F9FC] border-b border-[#C8D4E8] pt-20 pb-24 lg:pt-28 lg:pb-32">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<!-- Context Pill -->
		<div class="inline-flex items-center gap-2 border border-[#C8D4E8] bg-white px-3.5 py-1 text-xs font-mono font-medium text-[#1446A0] shadow-xs mb-8">
			<span class="inline-block h-2 w-2 bg-[#D4831A]"></span>
			<span class="uppercase tracking-wider">South Africa · Social Impact Workforce Platform</span>
		</div>

		<!-- Main Catchphrase Header (Claude-inspired Serif) -->
		<h1 class="font-serif text-5xl sm:text-7xl lg:text-8xl font-normal tracking-tight text-[#0D1B3E] max-w-5xl leading-[1.08]">
			Work. Income. <br />
			<span class="italic text-[#1446A0]">Experience.</span> Guaranteed.
		</h1>

		<p class="mt-8 max-w-2xl text-lg sm:text-xl text-[#465A7A] leading-relaxed font-sans font-light">
			Connecting youth program alumni to South African SMEs for paid tasks. Corporates fund the youth wages, SMEs get free talent, and the
			<span class="font-medium text-[#0D1B3E]">Experience Ledger</span> records verifiable proof of capability.
		</p>

		<!-- Call to Action Buttons (Sharp Edges) -->
		<div class="mt-10 flex flex-wrap items-center gap-4">
			{#if loggedIn}
				<a
					href={dashboardUrl}
					class="cape-btn-primary px-8 py-3.5 text-sm font-mono uppercase tracking-wider shadow-xs"
				>
					<span>Open Dashboard →</span>
				</a>
			{:else}
				<a
					href="/register"
					class="cape-btn-primary px-8 py-3.5 text-sm font-mono uppercase tracking-wider shadow-xs"
				>
					<span>Register as Youth or SME →</span>
				</a>
				<a
					href="/login"
					class="cape-btn-secondary px-8 py-3.5 text-sm font-mono uppercase tracking-wider shadow-xs"
				>
					Sign In
				</a>
			{/if}

			<a
				href="/tasks"
				class="border border-[#C8D4E8] bg-white px-6 py-3.5 text-sm font-mono uppercase tracking-wider text-[#1446A0] hover:bg-[#EFF4FF] transition-colors"
			>
				Explore Tasks
			</a>
		</div>

		<!-- The Triple Guarantee Badges (Sharp Editorial Boxes) -->
		<div class="mt-16 grid grid-cols-1 md:grid-cols-3 gap-6 max-w-5xl">
			<div class="border border-[#C8D4E8] bg-white p-6 shadow-xs border-t-2 border-t-[#1446A0]">
				<div class="font-mono text-xs font-bold text-[#1446A0] uppercase tracking-widest mb-2">
					Guarantee 01
				</div>
				<h2 class="font-serif text-2xl text-[#0D1B3E] font-medium mb-2">Work</h2>
				<p class="text-sm text-[#465A7A] leading-relaxed font-sans">
					SSH actively matches youth to tasks. If no SME tasks exist, SSH creates internal tasks to ensure guaranteed activity.
				</p>
			</div>

			<div class="border border-[#C8D4E8] bg-white p-6 shadow-xs border-t-2 border-t-[#D4831A]">
				<div class="font-mono text-xs font-bold text-[#D4831A] uppercase tracking-widest mb-2">
					Guarantee 02
				</div>
				<h2 class="font-serif text-2xl text-[#0D1B3E] font-medium mb-2">Income</h2>
				<p class="text-sm text-[#465A7A] leading-relaxed font-sans">
					Corporates fund youth wages. SSH pays youth directly. SMEs never handle cash, removing payroll disputes.
				</p>
			</div>

			<div class="border border-[#C8D4E8] bg-white p-6 shadow-xs border-t-2 border-t-[#1A8A55]">
				<div class="font-mono text-xs font-bold text-[#1A8A55] uppercase tracking-widest mb-2">
					Guarantee 03
				</div>
				<h2 class="font-serif text-2xl text-[#0D1B3E] font-medium mb-2">Verified Experience</h2>
				<p class="text-sm text-[#465A7A] leading-relaxed font-sans">
					The Experience Ledger records every verified task. Youth exit with portable, employer-trusted proof.
				</p>
			</div>
		</div>
	</div>
</section>

<!-- The Three-Party Model Section -->
<section class="py-20 bg-white border-b border-[#C8D4E8]">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="max-w-3xl mb-16">
			<span class="font-mono text-xs font-bold uppercase tracking-widest text-[#1446A0]">Equitable Ecosystem</span>
			<h3 class="font-serif text-3xl sm:text-5xl text-[#0D1B3E] mt-2">
				The Three-Party Model
			</h3>
			<p class="mt-4 text-[#465A7A] text-base leading-relaxed">
				How SSH aligns corporates, SMEs, and young South Africans without exploitation or friction.
			</p>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-3 gap-8">
			<!-- 1. Youth Alumni -->
			<div class="border border-[#C8D4E8] bg-[#F8F9FC] p-8 flex flex-col justify-between hover:border-[#1446A0] transition-colors">
				<div>
					<div class="font-mono text-xs font-bold uppercase tracking-widest text-[#1446A0] mb-2">
						Participant
					</div>
					<h4 class="font-serif text-2xl text-[#0D1B3E] mb-4">Youth Program Alumni</h4>
					<ul class="space-y-3 text-sm text-[#465A7A] mb-8 font-sans">
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Access paid micro-tasks suited to trained skills</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Guaranteed corporate wages paid directly into wallet</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Build portable, employer-trusted Experience Ledger</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Remain active until securing permanent placement</span>
						</li>
					</ul>
				</div>
				<div class="pt-6 border-t border-[#C8D4E8]">
					<a href="/register" class="font-mono text-xs font-bold uppercase tracking-wider text-[#1446A0] hover:underline">
						Register as Youth →
					</a>
				</div>
			</div>

			<!-- 2. SMEs -->
			<div class="border-2 border-[#1446A0] bg-white p-8 flex flex-col justify-between shadow-md relative">
				<div class="absolute -top-3 right-6 bg-[#D4831A] text-white px-2.5 py-0.5 font-mono text-[10px] font-bold uppercase tracking-widest">
					Zero SME Cost
				</div>
				<div>
					<div class="font-mono text-xs font-bold uppercase tracking-widest text-[#D4831A] mb-2">
						Enterprise Partner
					</div>
					<h4 class="font-serif text-2xl text-[#0D1B3E] mb-4">South African SMEs</h4>
					<ul class="space-y-3 text-sm text-[#465A7A] mb-8 font-sans">
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Post operational tasks with zero placement or recruitment fees</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Get vetted capacity in tech, marketing, and finance</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Zero payroll liability — wages are funded externally</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Verify completed deliverables to unlock candidate payouts</span>
						</li>
					</ul>
				</div>
				<div class="pt-6 border-t border-[#C8D4E8]">
					<a href="/register" class="font-mono text-xs font-bold uppercase tracking-wider text-[#1446A0] hover:underline">
						Register SME Account →
					</a>
				</div>
			</div>

			<!-- 3. Corporates -->
			<div class="border border-[#C8D4E8] bg-[#F8F9FC] p-8 flex flex-col justify-between hover:border-[#1446A0] transition-colors">
				<div>
					<div class="font-mono text-xs font-bold uppercase tracking-widest text-[#1A8A55] mb-2">
						Capital &amp; Governance
					</div>
					<h4 class="font-serif text-2xl text-[#0D1B3E] mb-4">Corporate Sponsors</h4>
					<ul class="space-y-3 text-sm text-[#465A7A] mb-8 font-sans">
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Fund cohorts of youth directly via ESD / CSI capital</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Earn verified B-BBEE scorecard points and impact metrics</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Real-time auditable ledger of every Rand disbursed</span>
						</li>
						<li class="flex items-start gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Audit-ready compliance reporting on demand</span>
						</li>
					</ul>
				</div>
				<div class="pt-6 border-t border-[#C8D4E8]">
					<a href="/register" class="font-mono text-xs font-bold uppercase tracking-wider text-[#1446A0] hover:underline">
						Corporate Engagement →
					</a>
				</div>
			</div>
		</div>
	</div>
</section>

<!-- How It Works: 8-Step Flow -->
<section class="py-20 bg-[#F8F9FC] border-b border-[#C8D4E8]">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="max-w-3xl mb-16">
			<span class="font-mono text-xs font-bold uppercase tracking-widest text-[#1446A0]">Process Sequence</span>
			<h3 class="font-serif text-3xl sm:text-5xl text-[#0D1B3E] mt-2">
				How SSH Operates: The 8-Step Flow
			</h3>
			<p class="mt-4 text-[#465A7A] text-base">
				From corporate reserve funding to permanent employment exit.
			</p>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
			{#each steps as s}
				<div class="border border-[#C8D4E8] bg-white p-6 shadow-xs flex flex-col justify-between hover:border-[#1446A0] transition-colors">
					<div>
						<div class="flex items-center justify-between mb-4">
							<span class="font-mono text-2xl font-bold text-[#1446A0]">{s.step}</span>
							<span class="bg-[#EFF4FF] border border-[#C8D4E8] px-2 py-0.5 text-[10px] font-mono font-semibold text-[#1446A0] uppercase">
								{s.tag}
							</span>
						</div>
						<h4 class="font-serif text-lg text-[#0D1B3E] mb-2">{s.title}</h4>
						<p class="text-xs text-[#465A7A] leading-relaxed font-sans">{s.description}</p>
					</div>
				</div>
			{/each}
		</div>
	</div>
</section>

<!-- Call to Action Banner (Cape Cobalt Navy Surface #0A1E4A) -->
<section class="bg-[#0A1E4A] py-16 sm:py-20 text-white">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 text-center">
		<h3 class="font-serif text-3xl sm:text-5xl font-normal text-white">
			Guaranteed work. Verified proof. No friction.
		</h3>
		<p class="mx-auto mt-4 max-w-2xl text-sm sm:text-base text-[#C8D4E8] font-light">
			Join youth program alumni and growing South African SMEs on the Shared Services Hub today.
		</p>
		<div class="mt-8 flex flex-wrap items-center justify-center gap-4">
			<a
				href="/register"
				class="bg-[#D4831A] text-white px-8 py-3.5 text-xs font-mono uppercase tracking-widest font-bold hover:bg-[#b56c0f] transition-colors"
			>
				Create Account
			</a>
			<a
				href="/login"
				class="border border-[#C8D4E8] bg-transparent text-white px-8 py-3.5 text-xs font-mono uppercase tracking-widest font-bold hover:bg-white/10 transition-colors"
			>
				Sign In
			</a>
		</div>
	</div>
</section>
